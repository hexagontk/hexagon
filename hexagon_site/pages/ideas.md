
Auth https://shiro.apache.org/java-authentication-guide.html
Subject: uid (can be a system, not a person)
Credential: pass, public Key
Principal: personal data name, nick, birthday
Role:
Permission: action:type:id
User: physical human user

A component will have its credentials (as a user) to login in services
it will have a subject (its project name)

System:

Service: API, message broker
Application: are a kind of component which is final (meant for users)
WebApplication, Desktop Application, Mobile Application or Browser Aplication

Managers: singletons to manage across an application and/or between services
    EventManager
    SettingsManager
    TemplatesManager

TODO
Packaging and deployment

TODO
Add auto parsing/serializing of body/response based on a type
  Ie: Add get<Request, Response>("/path") {} (for all methods)
Add `Exchange.body/headers/params` as Sinatra. Ie: headers["Date"] = date
Streaming support if returning a stream

Render template if no route found and template with that name exist (after filter)

Server contains a list of RequestHandler (a router is only a list of RequestHandler)
