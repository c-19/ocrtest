/*
 * Copyright (c) 2018 - 2019, C19, all rights reserved.
 *
 * This software is licensed under under GPL-3.0-only or GPL-3.0-or-later, https://opensource.org/licenses/GPL-3.0
 */

package io.c19.ocr;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;

import java.util.Objects;

import static org.bytedeco.leptonica.global.lept.pixDestroy;
import static org.bytedeco.leptonica.global.lept.pixRead;

public class Ocr
{
    private final String language;
    private final String dataPath;
    private TessBaseAPI api;

    public Ocr( OcrConfig config )
    {
        this.dataPath = config.getDataPath();
        this.language = config.getLanguage();
    }

    public synchronized void init( )
    {
        if( this.api == null )
        {
            this.api = new TessBaseAPI();

            if (api.Init(dataPath, language) != 0) {
                throw new OcrInitialisationException("Failed to initialise Ocr.");
            }
        }
    }

    public synchronized void stop()
    {
        if( this.api != null )
        {
            this.api.End();
            this.api = null;
        }
    }

    public String getLanguage()
    {
        return language;
    }

    public String getDataPath()
    {
        return dataPath;
    }

    public String process( String image )
    {
        Objects.requireNonNull( image, "Image is null." );
        PIX pix = null;
        BytePointer outText = null;
        try {
            pix = pixRead(image);
            this.api.SetImage(pix);
            outText = this.api.GetUTF8Text();
            return outText.getString();
        }
        finally
        {
            if( pix != null )
            {
                pixDestroy( pix );
            }
            if( outText != null )
            {
                outText.deallocate();
            }
        }
    }


}
