/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr

import spock.lang.Specification

class OcrConfigTest extends Specification
{

    def "Set get language"()
    {
        given:
        OcrConfig config = new OcrConfig()
        config.language( "swe" ).dataPath( "/" )

        expect:
        config.getLanguage() == "swe"
        config.getDataPath() == "/"
    }
}
