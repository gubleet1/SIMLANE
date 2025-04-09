package ch.simlane.utils;

import ch.simlane.Simlane;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class SimlaneFileFilter extends FileFilter {

    private static final String SIMLANE_FILE_DESCRIPTION = "Simlane Map (*." + Simlane.FILE_EXTENSION + ")";

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String ext = getExtension(f);
        return ext != null && ext.equals(Simlane.FILE_EXTENSION);
    }

    @Override
    public String getDescription() {
        return SIMLANE_FILE_DESCRIPTION;
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}
