package org.eyeseetea.malariacare.domain.common

import org.junit.Assert
import org.junit.Test

class EitherShould {

    @Test
    fun `return right type if result is success`() {

        val obj = givenAMyObject()
        val successResult = Either.Right(obj)

        Assert.assertTrue(Either::class.java.isAssignableFrom(successResult::class.java))
        Assert.assertTrue(successResult.isRight)
        Assert.assertFalse(successResult.isLeft)
        successResult.fold({ Assert.fail() },
            { right ->
                Assert.assertTrue(right::class.java == MyObject::class.java)
                Assert.assertTrue(right == obj)
            })
    }

    @Test
    fun `return left type if result is fail`() {

        val failure = MyObjectFailure.NetworkConnection
        val failResult = Either.Left(failure)

        Assert.assertTrue(Either::class.java.isAssignableFrom(failResult::class.java))
        Assert.assertFalse(failResult.isRight)
        Assert.assertTrue(failResult.isLeft)
        failResult.fold({ left ->
            Assert.assertTrue(left::class.java == MyObjectFailure.NetworkConnection::class.java)
            Assert.assertTrue(left == failure)
        }, { Assert.fail() })
    }

    @Test
    fun `return mapped right type after map success result`() {

        val obj = givenAMyObject()
        val successResult = Either.Right(obj)

        val mappedResult = successResult.map { mapMyObject(it) }

        mappedResult.fold({ Assert.fail() },
            { right ->
                Assert.assertTrue(right.title.contains(" mapped"))
            })
    }

    @Test
    fun `return left type after map fail result`() {

        val failure = MyObjectFailure.ObjectNotFound
        val failResult = Either.Left(failure)

        val resultMapped = failResult.map { mapMyObject(it) }

        resultMapped.fold({ left -> Assert.assertTrue(left == failure) }, { Assert.fail() })
    }

    @Test
    fun `return mapped list right type after map a success list result`() {

        val objList = listOf(givenAMyObject("object 1"), givenAMyObject("object 2"))
        val objListSuccessResult = Either.Right(objList)

        val resultMapped = objListSuccessResult.map { it.map { mapMyObject(it) } }

        resultMapped.fold({ Assert.fail() },
            { right ->
                for (obj in right)
                    Assert.assertTrue(obj.title.contains(" mapped"))
            })
    }

    @Test
    fun `return expected value after flatmap children and map a success result`() {

        val obj = givenAMyObject()
        val successResult = Either.Right(obj)

        val numChildrenResultCase1 = successResult
            .flatMap { returnedObj ->
                getChildrenOfParent(returnedObj.id)
                    .map { actors -> actors.size }
            }

        val numChildrenResultCase2 = successResult
            .flatMap { returnedObj -> getChildrenOfParent(returnedObj.id) }
            .map { actors -> actors.size }

        numChildrenResultCase1.fold({ Assert.fail() },
            { right -> Assert.assertEquals(5, right) })

        numChildrenResultCase2.fold({ Assert.fail() },
            { right -> Assert.assertEquals(5, right) })
    }

    private fun getChildrenOfParent(parentId: Long): Either<MyObjectFailure, List<String>> {
        val interpretersList =
            listOf("Children 1", "Children 2", "Children 3", "Children 4", "Children 5")
        return Either.Right(interpretersList)
    }

    private fun givenAMyObject(title: String = "object 1"): MyObject =
        MyObject(1, title, "url", "overview")

    private fun mapMyObject(it: MyObject) =
        MyObject(it.id, it.title + " mapped", it.url, it.overview)
}

data class MyObject(val id: Long, val title: String, val url: String, val overview: String)

sealed class MyObjectFailure {
    object NetworkConnection : MyObjectFailure()
    object ObjectNotFound : MyObjectFailure()
}