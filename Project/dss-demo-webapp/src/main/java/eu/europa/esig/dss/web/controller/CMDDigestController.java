package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.editor.EnumPropertyEditor;
import eu.europa.esig.dss.web.model.*;
import eu.europa.esig.dss.web.service.CMDService;
import eu.europa.esig.dss.web.service.SigningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;


@Controller
@SessionAttributes(value = { "cmdSignatureDigestForm", "cmdOtpForm", "signedDocument" })
@RequestMapping(value = "/cmd-sign-a-digest")
public class CMDDigestController {

    private static final Logger LOG = LoggerFactory.getLogger(DigestController.class);

    private static final String SIGN_DIGEST = "cmd-digest";
    private static final String SIGNATURE_GET_OTP = "cmd-digest-get-otp";
    private static final String SIGNATURE_SIGNED = "cmd-digest-signed";
    //private static final String SIGNATURE_PROCESS = "nexu-signature-process";

    private static final String[] ALLOWED_FIELDS = { "signatureForm", "digestAlgorithm", "digestToSign", "documentName", "fileToCompute",
            "signatureLevel", "signWithExpiredCertificate", "addContentTimestamp" ,  "userId", "userPin", "userOtp"};

    @Autowired
    private SigningService signingService;

    @Autowired
    private CMDService cmdService;

    @Value("${default.digest.algo}")
    private String defaultDigestAlgo;



    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(SignatureForm.class, new EnumPropertyEditor(SignatureForm.class));
        webDataBinder.registerCustomEditor(SignatureLevel.class, new EnumPropertyEditor(SignatureLevel.class));
        webDataBinder.registerCustomEditor(DigestAlgorithm.class, new EnumPropertyEditor(DigestAlgorithm.class));
        webDataBinder.registerCustomEditor(EncryptionAlgorithm.class, new EnumPropertyEditor(EncryptionAlgorithm.class));
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder webDataBinder) {
        webDataBinder.setAllowedFields(ALLOWED_FIELDS);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showSignatureParameters(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentPrincipal = (User) authentication.getPrincipal();
        CMDSignatureDigestForm signatureDigestForm = new CMDSignatureDigestForm();
        signatureDigestForm.setDigestAlgorithm(DigestAlgorithm.forName(defaultDigestAlgo, DigestAlgorithm.SHA256));
        signatureDigestForm.setUserId(currentPrincipal.getPhone_number());
        model.addAttribute("cmdSignatureDigestForm", signatureDigestForm);
        return SIGN_DIGEST;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String sendSignatureParameters(Model model, HttpServletRequest response,
                                          @ModelAttribute("cmdSignatureDigestForm") @Valid CMDSignatureDigestForm signatureDigestForm, BindingResult result) {
        if (result.hasErrors()) {
            if (LOG.isDebugEnabled()) {
                List<ObjectError> allErrors = result.getAllErrors();
                for (ObjectError error : allErrors) {
                    LOG.debug(error.getDefaultMessage());
                }
            }
            return SIGN_DIGEST;
        }

        List<String> certificates;
        try {
            certificates = cmdService.getCertificatesOf(signatureDigestForm.getUserId());
        } catch(SOAPFaultException | CertificateException | IOException e) {
            e.printStackTrace();
            result.addError(new ObjectError("userId", "UserId is not valid!"));
            return SIGN_DIGEST;
        }

        // Set the user's certificates on the form object
        signatureDigestForm.setBase64Certificate(certificates.get(0));
        signatureDigestForm.setBase64CertificateChain(certificates.subList(1, certificates.size()));
        CertificateToken signingCertificate = DSSUtils.loadCertificateFromBase64EncodedString(certificates.get(0));
        signatureDigestForm.setEncryptionAlgorithm(EncryptionAlgorithm.forName(signingCertificate.getPublicKey().getAlgorithm()));
        signatureDigestForm.setSigningDate(new Date());

        if (signatureDigestForm.isAddContentTimestamp()) {
            signatureDigestForm.setContentTimestamp(WebAppUtils.fromTimestampToken(signingService.getContentTimestamp(signatureDigestForm)));
        }

        // Create a signing request
        String docName = signatureDigestForm.getDocumentName();
        ToBeSigned dataToSign = signingService.getDataToSign(signatureDigestForm);
        if (dataToSign == null) {
            return SIGN_DIGEST;
        }

        SignatureAlgorithm certificateSignatureAlgorithm = signingCertificate.getSignatureAlgorithm();

        String processId = cmdService.sign(docName,
                dataToSign.getBytes(),
                certificateSignatureAlgorithm.getEncryptionAlgorithm(),
                signatureDigestForm.getDigestAlgorithm(),
                signatureDigestForm.getUserId(),
                signatureDigestForm.getUserPin());

        signatureDigestForm.setProcessId(processId);

        // Request OTP from user
        CMDOTPForm cmdOtpForm = new CMDOTPForm();
        model.addAttribute("cmdOtpForm", cmdOtpForm);

        model.addAttribute("cmdSignatureDigestForm", signatureDigestForm);
        model.addAttribute("digestAlgorithm", signatureDigestForm.getDigestAlgorithm());
        return SIGNATURE_GET_OTP;
    }

   /* @RequestMapping(value = "/get-data-to-sign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public GetDataToSignResponse getDataToSign(Model model, @RequestBody @Valid DataToSignParams params,
                                               @ModelAttribute("cmdSignatureDigestForm") @Valid CMDSignatureDigestForm signatureDigestForm, BindingResult result) {
        signatureDigestForm.setBase64Certificate(params.getSigningCertificate());
        signatureDigestForm.setBase64CertificateChain(params.getCertificateChain());
        CertificateToken signingCertificate = DSSUtils.loadCertificateFromBase64EncodedString(params.getSigningCertificate());
        signatureDigestForm.setEncryptionAlgorithm(EncryptionAlgorithm.forName(signingCertificate.getPublicKey().getAlgorithm()));
        signatureDigestForm.setSigningDate(new Date());

        if (signatureDigestForm.isAddContentTimestamp()) {
            signatureDigestForm.setContentTimestamp(WebAppUtils.fromTimestampToken(signingService.getContentTimestamp(signatureDigestForm)));
        }

        model.addAttribute("signatureDigestForm", signatureDigestForm);

        ToBeSigned dataToSign = signingService.getDataToSign(signatureDigestForm);
        if (dataToSign == null) {
            return null;
        }

        GetDataToSignResponse responseJson = new GetDataToSignResponse();
        responseJson.setDataToSign(DatatypeConverter.printBase64Binary(dataToSign.getBytes()));
        return responseJson;
    } */

    @RequestMapping(value = "/sign-document", method = RequestMethod.POST)
    public String signDigest(Model model,
                                           @ModelAttribute("cmdSignatureDigestForm") @Valid CMDSignatureDigestForm signatureDigestForm,
                                           @ModelAttribute("cmdOtpForm") @Valid CMDOTPForm cmdOtpForm, BindingResult result) {
        String signature = cmdService.validateOtp(signatureDigestForm.getProcessId(), cmdOtpForm.getUserOtp());
        if(signature == null) {
            return SIGNATURE_GET_OTP;
        }
        signatureDigestForm.setBase64SignatureValue(signature);

        DSSDocument document = signingService.signDigest(signatureDigestForm);
        InMemoryDocument signedDocument = new InMemoryDocument(DSSUtils.toByteArray(document), document.getName(), document.getMimeType());
        model.addAttribute("signedDocument", signedDocument);
        model.addAttribute("rootUrl", "sign-document");
        return SIGNATURE_SIGNED;
    }

    @RequestMapping(value = "/sign-document/download", method = RequestMethod.GET)
    public String downloadSignedFile(@ModelAttribute("signedDocument") InMemoryDocument signedDocument, HttpServletResponse response) {
        try {
            MimeType mimeType = signedDocument.getMimeType();
            if (mimeType != null) {
                response.setContentType(mimeType.getMimeTypeString());
            }
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + signedDocument.getName() + "\"");
            Utils.copy(new ByteArrayInputStream(signedDocument.getBytes()), response.getOutputStream());

        } catch (Exception e) {
            LOG.error("An error occurred while pushing file in response : " + e.getMessage(), e);
        }
        return null;
    }

    @ModelAttribute("signatureForms")
    public SignatureForm[] getSignatureForms() {
        return new SignatureForm[] { SignatureForm.XAdES, SignatureForm.CAdES, SignatureForm.JAdES };
    }

    @ModelAttribute("digestAlgos")
    public DigestAlgorithm[] getDigestAlgorithms() {
        DigestAlgorithm[] algos = new DigestAlgorithm[] { DigestAlgorithm.SHA1, DigestAlgorithm.SHA256, DigestAlgorithm.SHA384,
                DigestAlgorithm.SHA512 };
        return algos;
    }

    @ModelAttribute("isMockUsed")
    public boolean isMockUsed() {
        return signingService.isMockTSPSourceUsed();
    }

}
