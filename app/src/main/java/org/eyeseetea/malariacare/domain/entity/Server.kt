package org.eyeseetea.malariacare.domain.entity

data class Server @JvmOverloads constructor(
    val url: String,
    val name: String? = null,
    val logo: ByteArray? = null,
    val isConnected: Boolean = false,
    val classification: ServerClassification = ServerClassification.COMPETENCIES
) {

    init {
        require(url.isNotBlank()) { "url is required" }
    }

    fun changeToConnected(): Server {
        return this.copy(isConnected = true)
    }

    fun isDataCompleted(): Boolean = url != null && name != null && logo != null

    // Array property in data class: it's recommended to override equals() / hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Server

        if (url != other.url) return false
        if (name != other.name) return false
        if (logo != null) {
            if (other.logo == null) return false
            if (!logo.contentEquals(other.logo)) return false
        } else if (other.logo != null) return false
        if (isConnected != other.isConnected) return false
        if (classification != other.classification) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (logo?.contentHashCode() ?: 0)
        result = 31 * result + isConnected.hashCode()
        result = 31 * result + classification.hashCode()
        return result
    }
}