title=Ideas
date=2017-04-22
type=page
status=published
~~~~~~


Auth https://shiro.apache.org/java-authentication-guide.html
Subject: uid (can be a system, not a person)
Credential: pass, public Key
Principal: personal data name, nick, birthday
Role:
Permission: action:type:id
User: physical human user

Settings/Configuration: a component will have its credentials (as a user) to login in services
it will have a subject (its project name)

System:

Components: API, message broker
Applications: are a kind of component which is final (meant for users)
WebApplication, Desktop Application, Mobile Application or Browser Aplication

For components:
Services: local or remote functionalities (interfaces), implementations will be *Client or
*Backend Ie:
    MessagingService <- RabbitMqClient, JeroMqClient
    StorageService <- RedisClient, MongoDbClient, CouchBaseClient

    SettingsService <- LocalSettingsBackend, EventSettingsBackend
    WebService <- ServletBackend, JettyServletBackend, UndertowBackend
    WebClientService <- OkHttpBackend
    SerializationService
    TemplateService

    MetricsService <- MetricsClient TODO
    RegistryService <- ConsulClient TODO

ServiceClient: implementations to access a concrete type of service
ComponentClient:

Managers: singletons to manage across an application and/or between services
    EventManager
    SettingsManager
    TemplatesManager

Packaging and deployment

TODO
`Exchange.call` method to redirect to other handler
`Router.install` to "insert" another route (modularity) (Maybe the same as `Router.mount`)
Add method to filters
Add metadata to routes (Swagger/RAML)
Add auto parsing/serializing of body/response based on a type
Add "Test" server implementation for tests without real networking
Add `Exchange.body/headers/params` as Sinatra. Ie: headers["Date"] = date
Streaming support if returning a stream

TODO Add base class for Application (setup locales, etc.) for web applications
TODO Add base class for Services (the setup for applications is not needed here)
TODO Add initialization for applications and services
TODO Setup metrics
