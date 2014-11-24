/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.CommonUtils;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Some helper methods for working with language codes, extracted from {@link LanguageCodePostProcessor}
 * @author Thomas Eckart
 */
public class LanguageCodeUtils {
    private final static Logger LOG = LoggerFactory.getLogger(LanguageCodeUtils.class);

    private static Map<String, String> twoLetterCodesMap;
    private static Map<String, String> threeLetterCodesMap;
    private static Map<String, String> silToIso639Map;
    private static Map<String, String> languageNameToIso639Map;
    private static Map<String, String> iso639ToLanguageNameMap;
    private static Map<String, String> iso639_2TToISO639_3Map;

    public static String getLanguageNameForLanguageCode(String langCode) {
        String result = getIso639ToLanguageNameMap().get(langCode);

        if (result == null) {
            result = langCode;
        }

        return result;
    }

    public static Map<String, String> getSilToIso639Map() {
        if (silToIso639Map == null) {
            silToIso639Map = createSilToIsoCodeMap();
        }
        return silToIso639Map;
    }

    public static Map<String, String> getTwoLetterCountryCodeMap() {
        if (twoLetterCodesMap == null) {
            twoLetterCodesMap = createCodeMap(MetadataImporter.config.getLanguage2LetterCodeComponentUrl());
        }
        return twoLetterCodesMap;
    }

    public static Map<String, String> getThreeLetterCountryCodeMap() {
        if (threeLetterCodesMap == null) {
            threeLetterCodesMap = createCodeMap(MetadataImporter.config.getLanguage3LetterCodeComponentUrl());
        }
        return threeLetterCodesMap;
    }

    public static Map<String, String> getLanguageNameToIso639Map() {
        if (languageNameToIso639Map == null) {
            languageNameToIso639Map = createReverseCodeMap(MetadataImporter.config.getLanguage3LetterCodeComponentUrl());
        }
        return languageNameToIso639Map;
    }

    public static Map<String, String> getIso639ToLanguageNameMap() {
        if (iso639ToLanguageNameMap == null) {
            iso639ToLanguageNameMap = createCodeMap(MetadataImporter.config.getLanguage3LetterCodeComponentUrl());
        }

        return iso639ToLanguageNameMap;
    }

    /**
     * Returns map of ISO 639-2/B codes to ISO 639-3
     *
     * It is strongly advised to use ISO 639-3 codes, the support for ISO 639-2
     * may be discontinued in the future
     *
     * @return map of ISO 639-2/B codes to ISO 639-3
     */
    public static Map<String, String> getIso6392TToISO6393Map() {
        if (iso639_2TToISO639_3Map == null) {
            iso639_2TToISO639_3Map = new HashMap<String, String>();
            iso639_2TToISO639_3Map.put("alb", "sqi");
            iso639_2TToISO639_3Map.put("arm", "hye");
            iso639_2TToISO639_3Map.put("baq", "eus");
            iso639_2TToISO639_3Map.put("bur", "mya");
            iso639_2TToISO639_3Map.put("cze", "ces");
            iso639_2TToISO639_3Map.put("chi", "zho");
            iso639_2TToISO639_3Map.put("dut", "nld");
            iso639_2TToISO639_3Map.put("fre", "fra");
            iso639_2TToISO639_3Map.put("geo", "kat");
            iso639_2TToISO639_3Map.put("ger", "deu");
            iso639_2TToISO639_3Map.put("gre", "ell");
            iso639_2TToISO639_3Map.put("ice", "isl");
            iso639_2TToISO639_3Map.put("max", "mkd");
            iso639_2TToISO639_3Map.put("mao", "mri");
            iso639_2TToISO639_3Map.put("may", "msa");
            iso639_2TToISO639_3Map.put("per", "fas");
            iso639_2TToISO639_3Map.put("rum", "ron");
            iso639_2TToISO639_3Map.put("slo", "slk");
            iso639_2TToISO639_3Map.put("tib", "bod");
            iso639_2TToISO639_3Map.put("wel", "cym");
        }

        return iso639_2TToISO639_3Map;
    }

    private static Map<String, String> createCodeMap(String url) {
        LOG.debug("Creating language code map.");
        try {
            Map<String, String> result = CommonUtils.createCMDIComponentItemMap(url);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
    }

    private static Map<String, String> createReverseCodeMap(String url) {
        LOG.debug("Creating language code map.");
        try {
            Map<String, String> result = CommonUtils.createReverseCMDIComponentItemMap(url);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
    }

    private static Map<String, String> createSilToIsoCodeMap() {
        LOG.debug("Creating silToIso code map.");
        try {
            Map<String, String> result = new HashMap<String, String>();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            URL url = new URL(MetadataImporter.config.getSilToISO639CodesUrl());
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(url.openStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("//lang", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String silCode = node.getFirstChild().getTextContent();
                String isoCode = node.getLastChild().getTextContent();
                result.put(silCode, isoCode);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
    }

}
