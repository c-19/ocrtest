/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr;

public class OcrConfig
{
    private String language = "eng";
    private String dataPath = null;

    public OcrConfig()
    {

    }

    public String getLanguage()
    {
        return language;
    }

    public String getDataPath()
    {
        return dataPath;
    }

    public OcrConfig language(String language )
    {
        this.language = language;
        return this;
    }

    public OcrConfig dataPath( String dataPath )
    {
        this.dataPath = dataPath;
        return this;
    }
}
