#!/usr/bin/env python

import sys
import os
import time
import logging
import json
import base64
import getopt
import threading
import re

import yaml
import requests
import pika


logging.basicConfig(level=logging.DEBUG)
log = logging.getLogger(__name__)
config = None


def main():
    global config
    try:
        opts, args = getopt.getopt(sys.argv[1:], 'hc:')
    except getopt.GetoptError:
        print sys.argv[0] + ': [-c <config_path>] [-h]'
        sys.exit(1)

    config_file = 'config/mock_backend.yaml'
    for opt, arg in opts:
        if opt == '-h':
            print 'usage: ' + sys.argv[0] + ' -c <config_path>'
            sys.exit(0)
        if opt == '-c':
            config_file = arg

    with open(config_file, 'r') as fp:
        config = yaml.load(fp)

    heartbeat_thread = HeartBeatThread(1, 'Thread-Heartbeat-1', 1)
    try:
        bunny = MockBackend(auth=config['rabbitmq_auth'], root_id=None)
        # bunny = MockBackend(dev=True)
        bunny.load_cache(config['cache_directory'])
        bunny.connect()
        heartbeat_thread.setBackend(bunny)
        heartbeat_thread.start()
        bunny.loop()
        bunny.disconnect()
        heartbeat_thread.stop()

    except (KeyboardInterrupt, SystemExit):
        log.debug('Caught exception')
        heartbeat_thread.stop()
    finally:
        heartbeat_thread.stop()


class MissingJobException(Exception):
    pass


class HeartBeatThread(threading.Thread):
    backend = None
    stopped = False

    def __init__(self, threadID, name, counter):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.counter = counter

    def setBackend(self, backend):
        self.backend = backend

    def run(self):
        log.debug('starting heartbeat thread: ' + str(self.name))
        while True and not self.stopped:
            self.backend.send_heartbeat()
            # done like this to ease exiting
            for i in range(0, int(config['engine_heartbeat_interval'])/2):
                time.sleep(2)
                if self.stopped:
                    break

    def stop(self):
        self.stopped = True


class Backend(object):
    def __init__(self, url=None, mq_host=None, auth=None, dev=False):

        self.url = url or config['bunny_engine_url']
        self.mq_host = None if dev else mq_host or config['rabbitmq_host']
        self.mq_credentials = pika.credentials.PlainCredentials(*auth.split(':')) if auth else None
        self.dev = dev
        self.backend_info = None
        self.conn = None
        self.conn_output = None
        self.channel = None
        self.channel_output = None
        self.backend_id = None

    def connect(self):
        payload = {
            'type': 'RABBIT_MQ'
        }
        r = requests.post(self.url + '/v0/engine/backends', json=payload)
        log.debug('Backend registration response %s: %s', r.status_code, r.content)
        r.raise_for_status()
        self.backend_info = r.json()
        be_cfg = self.backend_info['backend_configuration']
        eng_cfg = self.backend_info['engine_configuration']
        self.backend_id = self.backend_info['id']

        params = pika.ConnectionParameters(
            host=self.mq_host,
            #credentials=self.mq_credentials,
            #virtual_host=config['rabbitmq_vhost'] if not self.dev else None,
            heartbeat_interval=30
        )

        self.conn = pika.BlockingConnection(params)
        self.channel = self.conn.channel()
        self.channel.exchange_declare(exchange=eng_cfg['exchange'], type=eng_cfg['exchange_type'], durable=True)
        self.channel.queue_declare('jobs', durable=True)
        self.channel.queue_declare('control', durable=True)
        try:
            self.channel.queue_bind(exchange=be_cfg['exchange'],
                                    queue='jobs',
                                    routing_key=be_cfg['receive_routing_key'])
        except Exception as e:
            log.exception('exception caught: ' + str(e) + ', exchange not found')

        self.channel.queue_bind(exchange=be_cfg['exchange'],
                                queue='control',
                                routing_key=be_cfg['receive_control_routing_key'])

        log.debug('!!!!!!!!!!!!!!! starting consume')
        self.channel.basic_consume(self.callback_jobs, queue='jobs')
        self.channel.basic_consume(self.callback_control, queue='control')

    def disconnect(self):
        self.conn.close()

    def send_heartbeat(self):
        eng_cfg = self.backend_info['engine_configuration']
        hb = {'id': self.backend_info['id'], 'timestamp': int(round(time.time() * 1000))}
        log.debug('... bunny heartbeat')
        self.channel.basic_publish(exchange=eng_cfg['exchange'],
                                   routing_key=eng_cfg['heartbeat_routing_key'],
                                   body=json.dumps(hb))

    def send_job(self, job):
        eng_cfg = self.backend_info['engine_configuration']
        self.channel.basic_publish(exchange=eng_cfg['exchange'],
                                   routing_key=eng_cfg['receive_routing_key'],
                                   body=json.dumps(job))
        log.debug('sent job to {}, key: {}'.format(eng_cfg['exchange'], eng_cfg['receive_routing_key']))

    def callback_jobs(self, ch, method, properties, body):
        self.on_receive_job(json.loads(body))
        ch.basic_ack(delivery_tag=method.delivery_tag)

    def callback_control(self, ch, method, properties, body):
        self.on_receive_control(json.loads(body))
        ch.basic_ack(delivery_tag=method.delivery_tag)

    def loop(self):
        self.channel.start_consuming()

    def on_receive_job(self, job):
        pass

    def on_receive_control(self, msg):
        pass


class MockBackend(Backend):
    def __init__(self, *args, **kwargs):
        self.root = kwargs.pop('root_id', None)
        self.cache = {}
        self.cache_path = None
        self.total = 0
        Backend.__init__(self, *args, **kwargs)

    def on_receive_job(self, job):
        log.debug('!!!!!!!!!!!!!!!!!!!! job received: ' + str(job['name']))
        if self.root and self.root != job['rootId']:
            log.debug('Ignoring job %s from %s' % (job['name'], job['rootId']))
            return
        self.total += 1

        app = {}
        try:
            app_str = base64.b64decode(job['app'].split(',')[1])
            if not app_str:
                raise 'no app'
            app = json.loads(app_str)
        except Exception as e:
            log.exception(e)

        try:
            log.debug('{}, {}, {}'.format(self.total, job['id'], job['name']))

            if job['name'] not in self.cache:
                raise MissingJobException('Missing job in cache: %s', job['name'])
            job['outputs'] = self.cache[job['name']]
            job['status'] = 'COMPLETED'
            self.send_job(job)
            log.debug('sending job completed to engine')

        except MissingJobException as e:
            log.exception('missing job exception: ' + str(e))
            log.debug('Forcing mocked outputs')
            job['outputs'] = self.mock_output(app['outputs'])
            job['status'] = 'COMPLETED'

            # root/. is to hide the file
            dirname = str('cache/' + job['name']).replace('root.', 'root/.')
            # convert .1-9999 to /1-9999
            if re.search(r'\.([0-9]+)$', dirname):
                dirname = re.sub(r'\.([0-9]+)$', r'.meta/\1', dirname)
            else:
                dirname += '.meta'

            try:
                os.makedirs(dirname)
            except:
                pass

            fpw = open(dirname + '/job.json', 'w')
            fpw.write(json.dumps(job['outputs']))
            fpw.close()
            fpw = open(dirname + '/_mock', 'w')
            fpw.write('')
            fpw.close()

            log.debug('sending job completed to engine (missing cache entry)')
            self.send_job(job)
            self.reload_cache()

        except Exception as e:
            log.exception(e)

    def reload_cache(self):
        log.debug('Rebuilding cache...')
        self.load_cache(self.cache_path)

    def load_cache(self, cache_path):
        self.cache_path = cache_path

        for path, dirs, files in os.walk(cache_path):
            if 'job.json' not in files:
                continue
            name = path[len(cache_path)+1:].replace('/', '.').replace('.meta', '').replace('..', '.')
            try:
                with open(os.path.join(path, 'job.json')) as fp:
                    self.cache[name] = json.load(fp)
                    if not self.cache[name]:
                        log.debug('Empty outputs for', name)
            except Exception as e:
                log.debug('filename: ' + name)
                log.exception(e)
                continue

    def mock_output(self, outputs):
        result = {}
        mocks = {}

        mocks_file = config['mocks_file'] if config['mocks_file'] else 'config/mocks.json'
        with open(mocks_file, 'r') as fp:
            mocks = json.load(fp)

        print(outputs)
        for output in outputs:
            id = output['id'].replace('#', '')
            try:
                output['type'].remove('null')
            except Exception:
                pass

            if isinstance(output['type'], dict):
                type = output['type']['type']
            elif isinstance(output['type'], list):
                type = output['type'][0]
                if isinstance(type, dict):
                    type = type['type']

            else:
                type = output['type']

            result[id] = mocks[type]

        return result

if __name__ == '__main__':
    main()


