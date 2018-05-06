
@Device
Feature:
  Adding a device records it in the database

  Scenario Outline: Add a device
    Given a <deviceType> type
     When a client calls the API with a POST
     Then the service returns <resultCode>

    Examples:
      | deviceType | resultCode |
      | valid      | 201        |
      | invalid    | 400        |
