import pika

auth = 'bunny:bunny'
mq_credentials = pika.credentials.PlainCredentials(*auth.split(':'))

params = pika.ConnectionParameters(
        host='bunny-vayu.sbgenomics.com',
        credentials=mq_credentials,
        virtual_host='bunny')

fp = open('sample/app.json', 'r')
sample_json = fp.read()

connection = pika.BlockingConnection(params)
channel = connection.channel()

channel.exchange_declare(exchange='backend_exchange_d1abee3f-af71-4f5f-bd00-942ea4a50036', type='direct')

channel.basic_publish(exchange='backend_exchange_d1abee3f-af71-4f5f-bd00-942ea4a50036',
                      routing_key='receive_routing_key', body=sample_json)

connection.close()

print 'no errors, sent'
