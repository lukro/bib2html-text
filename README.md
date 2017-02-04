# bib2html-text
Converts a set of .bib-files by use of one or more .csl-file(s) to HTML or simple text. 'bib2html-text' is a client-server-application using RabbitMQ for task-management.
The conversion is done by scalable MicroServices. They can run either on the same machine as the server or on a seperate machine
Likewise, the client can run on the same machine as the server.

###System Requirements
This program was engineered to work best with Linux systems, however since all components are cross-platform compatible
you can also try running the components with other operating systems.

####Server
If the MicroServices run on a seperate machine, almost any configuration having at least 512MB RAM is sufficient.
Otherwise, follow the section "MicroServices".

####Client
Almost any configuration will be able to run the Client. 
Screen resolution of at least 1280*720 recommended.

####MicroServices
CPU : 1 Core per Service recommended
RAM : 512MB per Service recommended
MEM : SSD recommended

###Installation procedure (on all involved machines)
1. (Only for the machine(s) running the MicroServices) Install Pandoc: http://pandoc.org/installing.html
2. Install Erlang version 19.2.: http://www.erlang.org/downloads
3. Install RabbitMQ version 3.6: https://www.rabbitmq.com/download.html
4. Configure RabbitMQ to allow communication with required hosts. (see http://www.rabbitmq.com/networking.html)
   For this purpose, setup the rabbitmq.config (/etc/rabbitmq/rabbitmq.config on Linux) on all involved machines
   like this:
    ```
        [
         {rabbit, [
           {loopback_users, []}
         ]}
        ].
    ```
5. Make sure you have the latest version of java installed and set up (java.com/download)
6. Download jars from /deployment/ directory in this git branch

###Setting up the Server
1. Place 'microservice.jar' in the servers working directory if you intend to start services from the server.
2. Start the server.jar. Add at least one MicroService. You can do that either by pressing the button or
    starting the microservice.jar on a machine with RabbitMQ installed.
    The command for this is (where 192.168.2.100 is the ip address of the server):
    ```
        java -jar microservice.jar 192.168.2.100
    ```
3. Insert the secret key(s) into the secretkeys.txt (one line per key). You can also do that later on demand.
   You need to tell the client the secret key for connection. All machines with a valid secret key will be able to
   connect to the server.

###Creating a Request on the Client Machine:
1. Start the client.jar.
2. Add the necessary files on the left hand side:
    1. Add one or more BIB files
    2. Optionally, you can add some templates and CSL files. If not specified, the standard csl and template is used.
3. Enter the IP address of the Server on the right hand side and connect to it. If the Server runs on the same machine, you don not have to specify an IP Address.
4. Set your secret key.
5. Choose the Directory, where the output files go to. Preferably, select a directory you can write to without admin rights.

### Notes
It is not recommended to run too many services on a single machine.
You can blacklist a client by putting its IP address in the file blacklist.txt which is created in the directory
of the server.jar after running for the first time.