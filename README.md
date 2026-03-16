# kfrei-mvg-eai

[![Made with love by it@M][made-with-love-shield]][itm-opensource]
<!-- feel free to add more shields, style 'for-the-badge' -> see https://shields.io/badges -->

EAI component for communication between MVG (Münchner Verkehrsgesellschaft) and Kfrei (Kostenfreiheit des Schulwegs).

### Built With

The documentation project is built with technologies we use in our projects:

* Java 21
* Spring Boot 4
* Maven - Build Tool
* MapStruct - For object mapping
* Lombok - For reducing boilerplate code
* Springdoc OpenAPI - For API documentation
* WireMock - For testing (mocking HTTP requests)

## Set up

To start and run this project, you need to create an `application-local.yaml` configuration file and launch the application with the local profile. This
configuration file should include the following settings:

```
# OAuth2 Client ID with the necessary permissions to access the EAI API
app.resourceserver.client-id=example-id

# OAuth2 Token URL for generating a real token, used for request testing in Swagger UI
app.swagger-ui.token-url=https://sso.example.com/auth/realms/example/protocol/openid-connect/token

# OAuth2 configuration for the REST client to access the backend application
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://sso.example.com/auth/realms/example
spring.security.oauth2.client.provider.kfrei-rest-api.issuer-uri=https://sso.example.com/auth/realms/example
spring.security.oauth2.client.registration.kfrei-rest-api.client-id=example-id
spring.security.oauth2.client.registration.kfrei-rest-api.client-secret=example-secret
```

Alternatively, you can run the EAI component with the following profiles to disable security and use a mocked backend application. These options allow you
to test the project without security restrictions or with a mock setup.

* `no-security-kfrei-mvg-eai`: Disables security for the EAI component.
* `no-security-kfrei-rest-api`: Disables security for the REST client connecting to the backend application.
* `mock-kfrei-rest-api`: Uses a mock version of the backend application.

You can expect the following behavior when using `KfreiRestApiServiceMock`:

| HTTP-Request | Parameter                                     | Reponse Code | Content                                             | Note                                                   |
|--------------|-----------------------------------------------|--------------|-----------------------------------------------------|--------------------------------------------------------|
| GET          | antragId=1111111111, geburtsdatum=1989-11-30  | 200          | berechtigungAb=2024-02-29, befristungBis=2024-12-29 |                                                        |
| GET          | antragId=2222222222, geburtsdatum=1989-02-28  | 200          | berechtigungAb=2025-02-28, befristungBis=2025-12-28 |                                                        |
| GET          | antragId=3333333333, geburtsdatum=1989-09-30  | 200          | berechtigungAb=2026-01-30, befristungBis=2026-12-31 |                                                        |
| GET          | antragId=4444444444, geburtsdatum=1989-10-31  | 200          | berechtigungAb=2027-01-30, befristungBis=2027-12-31 |                                                        |
| GET          |                                               | 404          | none                                                | Any other combination of `antragId` and `geburtsdatum` |
| GET          | antragId=1111111111, geburtsdatum=d1989-11-30 | 404          | Invalid argument [propertyName=geburtsdatum]        | Wrong format in `geburtsdatum`                         |
| GET          | antragId=0                                    | 500          | none                                                | Simulates Internal Server Error in backend application |

You can access the Swagger OpenAPI documentation at http://localhost:8081/swagger-ui.html.

## Documentation

<!-- TODO -->

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated
**.

If you have a suggestion that would make this better, please open an issue with the tag "enhancement", fork the repo and create a pull request. You can also
simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Open an issue with the tag "enhancement"
2. Fork the Project
3. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
4. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
5. Push to the Branch (`git push origin feature/AmazingFeature`)
6. Open a Pull Request

More about this in the [CODE_OF_CONDUCT](/CODE_OF_CONDUCT.md) file.

## License

Distributed under the MIT License. See [LICENSE](LICENSE) file for more information.

## Contact

it@M - opensource@muenchen.de

<!-- project shields / links -->

[made-with-love-shield]: https://img.shields.io/badge/made%20with%20%E2%9D%A4%20by-it%40M-yellow?style=for-the-badge

[itm-opensource]: https://opensource.muenchen.de/