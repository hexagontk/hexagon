package com.hexagontk.http.model

typealias Header = Field
typealias Parameter = Field

const val CONTINUE_100: Int = 100
const val SWITCHING_PROTOCOLS_101: Int = 101
const val PROCESSING_102: Int = 102
const val EARLY_HINTS_103: Int = 103

const val OK_200: Int = 200
const val CREATED_201: Int = 201
const val ACCEPTED_202: Int = 202
const val NON_AUTHORITATIVE_INFORMATION_203: Int = 203
const val NO_CONTENT_204: Int = 204
const val RESET_CONTENT_205: Int = 205
const val PARTIAL_CONTENT_206: Int = 206
const val MULTI_STATUS_207: Int = 207
const val ALREADY_REPORTED_208: Int = 208
const val IM_USED_226: Int = 226

const val MULTIPLE_CHOICES_300: Int = 300
const val MOVED_PERMANENTLY_301: Int = 301
const val FOUND_302: Int = 302
const val SEE_OTHER_303: Int = 303
const val NOT_MODIFIED_304: Int = 304
const val USE_PROXY_305: Int = 305
const val TEMPORARY_REDIRECT_307: Int = 307
const val PERMANENT_REDIRECT_308: Int = 308

const val BAD_REQUEST_400: Int = 400
const val UNAUTHORIZED_401: Int = 401
const val PAYMENT_REQUIRED_402: Int = 402
const val FORBIDDEN_403: Int = 403
const val NOT_FOUND_404: Int = 404
const val METHOD_NOT_ALLOWED_405: Int = 405
const val NOT_ACCEPTABLE_406: Int = 406
const val PROXY_AUTHENTICATION_REQUIRED_407: Int = 407
const val REQUEST_TIMEOUT_408: Int = 408
const val CONFLICT_409: Int = 409
const val GONE_410: Int = 410
const val LENGTH_REQUIRED_411: Int = 411
const val PRECONDITION_FAILED_412: Int = 412
const val CONTENT_TOO_LARGE_413: Int = 413
const val URI_TOO_LONG_414: Int = 414
const val UNSUPPORTED_MEDIA_TYPE_415: Int = 415
const val RANGE_NOT_SATISFIABLE_416: Int = 416
const val EXPECTATION_FAILED_417: Int = 417
const val I_AM_A_TEAPOT_418: Int = 418
const val MISDIRECTED_REQUEST_421: Int = 421
const val UNPROCESSABLE_CONTENT_422: Int = 422
const val LOCKED_423: Int = 423
const val FAILED_DEPENDENCY_424: Int = 424
const val TOO_EARLY_425: Int = 425
const val UPGRADE_REQUIRED_426: Int = 426
const val PRECONDITION_REQUIRED_428: Int = 428
const val TOO_MANY_REQUESTS_429: Int = 429
const val REQUEST_HEADER_FIELDS_TOO_LARGE_431: Int = 431
const val UNAVAILABLE_FOR_LEGAL_REASONS_451: Int = 451

const val INTERNAL_SERVER_ERROR_500: Int = 500
const val NOT_IMPLEMENTED_501: Int = 501
const val BAD_GATEWAY_502: Int = 502
const val SERVICE_UNAVAILABLE_503: Int = 503
const val GATEWAY_TIMEOUT_504: Int = 504
const val HTTP_VERSION_NOT_SUPPORTED_505: Int = 505
const val VARIANT_ALSO_NEGOTIATES_506: Int = 506
const val INSUFFICIENT_STORAGE_507: Int = 507
const val LOOP_DETECTED_508: Int = 508
const val NOT_EXTENDED_510: Int = 510
const val NETWORK_AUTHENTICATION_REQUIRED_511: Int = 511

val INFORMATION: IntRange = 100..199
val SUCCESS: IntRange = 200..299
val REDIRECTION: IntRange = 300..399
val CLIENT_ERROR: IntRange = 400..499
val SERVER_ERROR: IntRange = 500..599
