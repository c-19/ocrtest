/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class OcrTest extends Specification
{
    @Shared Ocr instance
    @Shared OcrConfig config = new OcrConfig().language( "eng" )

    def setupSpec()
    {
        instance = new Ocr( config )
        instance.init()
    }

    def cleanupSpec()
    {
        instance.stop()
    }

    def "Initialise Tesseract"()
    {
        expect:
        instance.getLanguage() == "eng"
        instance.getDataPath() == null
    }

    def "Initialise multiple times"()
    {
        given:
        instance.init()

        when:
        instance.stop()

        and:
        instance.init()

        then:
        noExceptionThrown()
    }

    def "Initialise multiple instances"()
    {
        given:
        Ocr instance2 = new Ocr( config )

        when:
        instance2.init()

        then:
        noExceptionThrown()

        cleanup:
        instance2.stop()
    }

    @Unroll
    def "Process image to text"()
    {
        when:
        String actual = instance.process( image )

        then:
        actual.contains( expected )

        where:
        image || expected
        "src/test/resources/test00.png" || "Running Tesseract"
        "src/test/resources/test01.png" || "Other Platforms"
        "src/test/resources/test02.tif" || "Tesseract is a command-line program,"
    }
}
