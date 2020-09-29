package org.eyeseetea.malariacare.data.api

import org.eyeseetea.malariacare.common.ResourcesFileReader
import org.eyeseetea.malariacare.data.remote.api.ServerRemoteDataSource
import org.eyeseetea.malariacare.data.remote.poeditor.PoEditorApiClient
import org.eyeseetea.malariacare.domain.entity.ServerClassification
import org.eyeseetea.malariacare.rules.MockWebServerRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class ServerRemoteDataSourceShould {
    @Rule
    @JvmField
    var rule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    var mockWebServerRule = MockWebServerRule(ResourcesFileReader())

    @Test
    fun `return server list with competencies classification by server if classification does not exist`() {
        val dataSource = ServerRemoteDataSource(
            PoEditorApiClient(
                "AnyId",
                "AnyToken",
                mockWebServerRule.mockServer.baseEndpoint
            )
        )

        mockWebServerRule.mockServer.enqueueMockResponse(
            WITHOUT_CLASSIFICATION_SERVER_LIST_PO_EDITOR_RESPONSE
        )

        val serversResult = dataSource.getAll()

        serversResult.forEach {
            Assert.assertEquals(it.classification, ServerClassification.COMPETENCIES)
        }
    }

    @Test
    fun `return server list with competencies classification by server if classification is competencies`() {
        val dataSource = ServerRemoteDataSource(
            PoEditorApiClient(
                "AnyId",
                "AnyToken",
                mockWebServerRule.mockServer.baseEndpoint
            )
        )

        mockWebServerRule.mockServer.enqueueMockResponse(
            WITH_COMPETENCIES_CLASSIFICATION_SERVER_LIST_PO_EDITOR_RESPONSE
        )

        val serversResult = dataSource.getAll()

        serversResult.forEach {
            Assert.assertEquals(it.classification, ServerClassification.COMPETENCIES)
        }
    }

    @Test
    fun `return server list with scoring classification by server if classification is scoring`() {
        val dataSource = ServerRemoteDataSource(
            PoEditorApiClient(
                "AnyId",
                "AnyToken",
                mockWebServerRule.mockServer.baseEndpoint
            )
        )

        mockWebServerRule.mockServer.enqueueMockResponse(
            WITH_SCORING_CLASSIFICATION_SERVER_LIST_PO_EDITOR_RESPONSE
        )

        val serversResult = dataSource.getAll()

        serversResult.forEach {
            Assert.assertEquals(it.classification, ServerClassification.SCORING)
        }
    }

    @Test
    fun `return server list with competencies classification by server if classification is empty`() {
        val dataSource = ServerRemoteDataSource(
            PoEditorApiClient(
                "AnyId",
                "AnyToken",
                mockWebServerRule.mockServer.baseEndpoint
            )
        )

        mockWebServerRule.mockServer.enqueueMockResponse(
            WITH_EMPTY_CLASSIFICATION_SERVER_LIST_PO_EDITOR_RESPONSE
        )

        val serversResult = dataSource.getAll()

        serversResult.forEach {
            Assert.assertEquals(it.classification, ServerClassification.COMPETENCIES)
        }
    }

    companion object {
        private const val WITHOUT_CLASSIFICATION_SERVER_LIST_PO_EDITOR_RESPONSE =
            "without_classification_server_list_po_editor_response.json"
        private const val WITH_COMPETENCIES_CLASSIFICATION_SERVER_LIST_PO_EDITOR_RESPONSE =
            "with_competencies_classification_server_list_po_editor_response.json"
        private const val WITH_SCORING_CLASSIFICATION_SERVER_LIST_PO_EDITOR_RESPONSE =
            "with_scoring_classification_server_list_po_editor_response.json"
        private const val WITH_EMPTY_CLASSIFICATION_SERVER_LIST_PO_EDITOR_RESPONSE =
            "with_empty_classification_server_list_po_editor_response.json"
    }
}
