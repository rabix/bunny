import pika
import sys

auth = 'bunny:bunny'
mq_credentials = pika.credentials.PlainCredentials(*auth.split(':'))

params = pika.ConnectionParameters(
        host='localhost',
        credentials=mq_credentials,
        virtual_host='bunny')

connection = pika.BlockingConnection(params)
channel = connection.channel()

channel.exchange_declare(exchange='engine_exchange',
                         type='direct')

result = channel.queue_declare(exclusive=True)
queue_name = result.method.queue

# binding_keys = sys.argv[1:]
binding_keys = ['receive_routing_key']
if not binding_keys:
    sys.stderr.write("Usage: %s [binding_key]...\n" % sys.argv[0])
    sys.exit(1)

for binding_key in binding_keys:
    print('binding to ' + str(queue_name) + ', routing_key: ' + str(binding_key))
    channel.queue_bind(exchange='engine_exchange',
                       queue=queue_name,
                       routing_key=binding_key)

print(' [*] Waiting for logs. To exit press CTRL+C')


def callback(ch, method, properties, body):
    print(" [x] %r:%r" % (method.routing_key, body))

channel.basic_consume(callback,
                      queue=queue_name,
                      no_ack=True)

channel.start_consuming()
