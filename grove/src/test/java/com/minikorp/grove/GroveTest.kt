package com.minikorp.grove

import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GroveTest {

    @get:Rule val tmpFolder = TemporaryFolder()
    lateinit var fileLogTree: FileLogTree

    @Before
    fun before() {
        fileLogTree = FileLogTree(tmpFolder.newFile())
        Grove.plant(fileLogTree)
        Grove.forest.size `should be equal to` 1
    }

    @After
    fun after() {
        Grove.uproot(fileLogTree)
        Grove.forest.`should be empty`()
    }

    @Test
    fun logWrites() {
        val testString = "Test String"
        Grove.d { testString }
        Grove.d { "Second string" }
        fileLogTree.close()
        val writtenText = fileLogTree.file.readText()
        writtenText `should contain` testString
        writtenText.lines().filter { it.isNotEmpty() }.size `should be equal to` 2
    }
}