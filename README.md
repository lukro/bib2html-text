# bib2html-text
Converts a set of .bib-files by use of one or more .csl-file(s) to HTML or simple text. 'bib2html-text' is a client-server-application and uses RabbitMQ for task-management. More information coming soon.

###System Requirements
This program was engineered to work best with Linux systems, however since all components are cross-platform compatible
you can also try running the components with other operating systems.

####Server
Depending on the load... etc.

####Client
Almost any configuration will be able to run the Client. 
Screen resolution of at least 1280*720 recommended.

####MicroServices
CPU : 1 Core per Service Recommended
RAM : 512MB per Service Recommended
MEM : SSD Recommended

###Software Requirements
1. Install Pandoc v.123 (pandoc.com/download)
2. Install Erlang v.456 (erlang.com/download)
3. Install RabbitMQ v.789 (rabbitmq.com/download)
4. Configure RabbitMQ to allow communication with required hosts (see rabbitmq.com/manual/config)
5. Make sure you have the latest version of java installed and set up (java.com/download)
6. Download jars from /deployment/ directory in this git branch

###Installation procedure
1. Place 'microservice.jar' in the servers working directory if you intend to start services from the server
2. ... etc.

### Notes
It is not recommended to run too many services on a single machine ... etc.
You can create your own services... etc.