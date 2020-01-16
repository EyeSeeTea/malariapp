package org.eyeseetea.malariacare.data.remote.poeditor

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Date

class PoEditorResponse<T>(
    var response: Response,
    var result: T
)

data class Response(
    val status: String,
    val code: String,
    val message: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
class TermsResult(val terms: List<Term>?)

@JsonIgnoreProperties(ignoreUnknown = true)
class Term(
    val term: String,
    val created: Date,
    val translation: Translation? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Translation(val content: String)
