----------------------------------------------------------
|							 |
|			README				 |
|							 |
----------------------------------------------------------

* De forma a testar o projeto desenvolvido, é recomendado que a ordem da inicialização dos componentes seja respeitada, seguindo a ordem descrita abaixo. Caso utilize a Google Cloud Platform deverá ativar as funcionalidades referentes à Cloud Function e à Vision API. Deverá também ser instalado o contrato através da funcionalidade install do Maven, de forma ao cliente e servidor gRPC funcionarem corretamente e conseguirem comunicar entre si.

################################
##			      ##
##	Lookup Function	      ##
##			      ##
################################

* Comando para fazer o deployment da função na GCP, restando preencher com o e-mail da conta de serviço com as permissões necessárias, referidas mais abaixo:

	gcloud functions deploy lookup-function --allow-unauthenticated --entry-point=Entrypoint --runtime=java11 --trigger-http --region=europe-west1 --source=target/deployment --service-account=<todo-service-account-email>

* Conta de serviço - permissões (roles):
	- Compute Admin: Para aceder ao serviço Compute Engine

* Configurações:
	- Colocar o id do projeto correspondente na constante PROJECT da classe Entrypoint;

* Utilização:
	- Na aplicação cliente deve ser inserido o nome do instance group e a sua zona, de forma a que a Lookup Function consiga obter as VMs pretendidas.

################################
##			      ##
##	Monitor Function      ##
##			      ##
################################

* Comando para fazer o deployment da função na GCP, restando preencher com o e-mail da conta de serviço com as permissões necessárias, referidas mais abaixo:

	gcloud functions deploy monitor-function --entry-point=Entrypoint --runtime=java11 --trigger-topic detectionworkers --region=europe-west1 --source=target/deployment --service-account=<todo-service-account-email>

* Conta de serviço - permissões (roles):
	- Compute Admin: Para aceder ao serviço Compute Engine
	- Cloud Datastore Owner: Para aceder ao serviço Firestore

* Configurações:
	- Colocar o id do projeto correspondente na constante PROJECT da classe Entrypoint;
	- Criar um instance group com o nome 'detect-objects-app-instance-group' na zona 'europe-west1-b' ou então alterar os valores das constantes INSTANCE_GROUP_NAME e INSTANCE_GROUP_ZONE da classe Entrypoint com os valores desejados. 

* Utilização:
	- Caso a coleção Monitor ou o seu documento properties sejam apagados da Firestore ou não existam sequer, sempre que a função realizar as suas operações irá criar a coleção e o documento quando não existirem;
	- A função ao ser deployed e as configurações ao estarem corretas não é necessário realizar mais nenhuma operação, a função já se deverá encontrar a funcionar corretamente.

################################
##			      ##
##	  GRPC Server	      ##
##			      ##
################################

* Conta de serviço - permissões (roles):
	- Cloud Datastore Owner: Para aceder ao serviço Firestore
	- Storage Admin: Para aceder ao serviço Cloud Storage
	- Pub/Sub Admin: Para aceder ao serviço Cloud Pub/Sub

* Configurações:
	- Definir a variável de ambiente GOOGLE_APPLICATION_CREDENTIALS com o caminho para a o ficheiro .json correspondente à conta de serviço com as roles acima mencionadas.

* Utilização:
	- Ao iniciar o servidor, deve ser inserido nos argumentos a port a que este deve ficar associado;
	- Não sendo obrigatório, poderá ser passado como segundo argumento o nome do bucket que é pretendido utilizar, o qual deve estar previamente inicializado;
	- Não é necessário criar previamente nem o bucket nem o tópico para o qual vão ser publicadas mensagens, caso não existam aquando da inicialização do servidor, serão criados.

################################
##			      ##
##     Detect Objects App     ##
##			      ##
################################

* Conta de serviço - permissões (roles):
	- Cloud Datastore Owner: Para aceder ao serviço Firestore
	- Storage Admin: Para aceder ao serviço Cloud Storage
	- Pub/Sub Admin: Para aceder ao serviço Cloud Pub/Sub

* Configurações:
	- Definir a variável de ambiente GOOGLE_APPLICATION_CREDENTIALS com o caminho para a o ficheiro .json correspondente à conta de serviço com as roles acima mencionadas.

* Utilização:
	- Não é necessário criar previamente a subscrição utilizada pela aplicação, caso não exista aquando da inicialização da aplicação, mais especificamente, na parte da criação do subscriber, será criada.

################################
##			      ##
##	     Client	      ##
##			      ##
################################

* Para a aplicação Client não é necessário nenhuma conta de serviço, nem nenhuma configuração adicional, quando a aplicação for inicializada será pedido que seja inserido o nome e a zona do instance group onde correm as instâncias do servidor gRPC, caso não seja escolhido utilizar o localhost.


>-------------------------------<
Caso seja pretendido colocar o servidor gRPC e a Detect Objects App em VMs a correr na cloud poderão ser utilizados os scripts fornecidos em conjunto com este documento, para colocar as aplicações a correr e definir a variável de ambiente com a conta de serviço correspondente durante o startup das VMs. Os scrips a utilizar são: grpc-server-startup.sh e detect-objects-app-startup.sh, respetivamente. Ao utilizar os scripts, deve ser tido em conta que os servidores e contas de serviço devem estar na diretoria /var/server, criada com recurso ao comando 'sudo mkdir /var/server' realizado a partir da diretoria home.


######################################################################################

Realizado pelos alunos:
	-> João Arcanjo nº 47193
	-> Diogo Novo nº 47256

Licenciatura em Engenharia Informática e de Computadores no ISEL

Realizado no âmbito da unidade curricular de Computação na Nuvem

Semestre de verão 2021/22

9 de junho de 2022
