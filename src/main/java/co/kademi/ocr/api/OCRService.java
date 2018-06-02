package co.kademi.ocr.api;

import java.io.InputStream;

/**
 *
 * @author brad
 */
public interface OCRService {
    void registerListener( OCRListener l );

    void scanToTable(InputStream in, String jobId);
    
}
