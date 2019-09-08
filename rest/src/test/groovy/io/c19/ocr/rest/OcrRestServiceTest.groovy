/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr.rest

import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class OcrRestServiceTest extends Specification
{

    @Shared TestRestResource testRestResource
    @Shared OcrRestClient client = new OcrRestClient()

    def setupSpec()
    {
        testRestResource = new TestRestResource( )
        testRestResource.setUp()
        client.setTarget( testRestResource.target().getUri().toString())
    }

    def cleanupSpec()
    {
        testRestResource.tearDown()
    }

    @Unroll
    def "Post image as inputstream"()
    {
        given:
        InputStream input = new FileInputStream( image )

        when:
        String actual = client.ocr( input )

        then:
        actual.contains( expected )

        where:
        image || expected
        "src/test/resources/test00.png" || "Running Tesseract"
        "src/test/resources/test01.png" || "Other Platforms"
        "src/test/resources/test02.tif" || "Tesseract is a command-line program,"

    }

    class TestRestResource extends JerseyTest
    {

        @Override
        protected ResourceConfig configure()
        {
            ResourceConfig r = new ResourceConfig().register( OcrRestService.class )
            return r
        }

    }

}
