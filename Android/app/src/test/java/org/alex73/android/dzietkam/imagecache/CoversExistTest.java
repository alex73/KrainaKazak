package org.alex73.android.dzietkam.imagecache;

import org.alex73.android.dzietkam.CatalogLoader;
import org.alex73.android.dzietkam.catalog.Catalog;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.file.Paths;

public class CoversExistTest {
    @Test
    public void getAllCovers() throws Exception {
        System.out.println(Paths.get("").toAbsolutePath());
        Catalog catalog = CatalogLoader.load(new FileInputStream("src/main/res/raw/catalog.json"));
    }
}
