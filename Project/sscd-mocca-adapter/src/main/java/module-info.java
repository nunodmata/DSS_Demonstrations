

module eu.europa.esig.dss.token.mocca {
        requires java.smartcardio;
        requires smcc;
        requires jpms_dss_enumerations;
        requires jpms_dss_model;
        requires jpms_dss_spi;
        requires jpms_dss_token;
        requires org.slf4j;
        requires org.bouncycastle.provider;
        requires jdk.crypto.cryptoki;
        exports eu.europa.esig.dss.token.mocca;
}