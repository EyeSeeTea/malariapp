package org.eyeseetea.malariacare.domain.entity

import java.util.EnumSet
import java.util.HashMap

enum class ServerClassification(val code: Int) {
    SCORING(0), COMPETENCIES(1);

    companion object {
        private val lookup: MutableMap<Int, ServerClassification> =
            HashMap()

        operator fun get(code: Int): ServerClassification? {
            return lookup[code]
        }

        init {
            for (s in EnumSet.allOf(ServerClassification::class.java)) lookup[s.code] = s
        }
    }
}
