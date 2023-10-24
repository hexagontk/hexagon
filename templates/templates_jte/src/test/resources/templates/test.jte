@import java.net.URL
@import java.time.LocalDateTime

@param String key1
@param String key2
@param String a
@param LocalDateTime localDate
@param URL _template_
@param LocalDateTime _now_

<!DOCTYPE html>

<html>
<head>
  <title>Fortunes</title>
</head>
<body>
  <p>This is a test template</p>
  <p>${key1 == null? "key1" : key1}</p>
  <p>${key2 == null? "key2" : key2}</p>
  <p>${a == null? "a" : a}</p>
  <p>${localDate == null? "localDate" : localDate.toString()}</p>
  <p>${_template_ == null? "_template_" : _template_.toString()}</p>
  <p>${_now_ == null? "_now_" : _now_.toString()}</p>
</body>
</html>
