package eu.europa.esig.dss.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import eu.europa.esig.dss.web.model.AccountManagementForm;
import eu.europa.esig.dss.web.model.User;
import eu.europa.esig.dss.web.repository.UserRepository;

import org.springframework.ui.Model;

@Controller
@SessionAttributes(value = { "accountManagementForm" })
@RequestMapping(value = "/account-management")
public class AccountManagementController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountManagementController.class);

    @Autowired
    private UserRepository userRepository;

    //RequestMaping login
    @RequestMapping(method = RequestMethod.GET)
    public String getPage(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AccountManagementForm accountManagementForm = new AccountManagementForm();
        User currentPrincipal = (User) authentication.getPrincipal();
        accountManagementForm.setUserId(currentPrincipal.getPhone_number().equals("") ? "+351" : currentPrincipal.getPhone_number());
        model.addAttribute("accountManagementForm", accountManagementForm);

        return "account-management";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String updateNumber(Model model, HttpServletRequest request, @ModelAttribute("accountManagementForm") @Valid AccountManagementForm accountManagementForm) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User auth_user = (User) authentication.getPrincipal();
        User u_tmp = userRepository.updateNumbUser(accountManagementForm.getUserId(), ((User) authentication.getPrincipal()).getId());
        auth_user.setPhone_number(u_tmp.getPhone_number());
        LOG.info("form: " + accountManagementForm.getUserId());
        LOG.info("User updated: " + u_tmp.getId() + " " + u_tmp.getPhone_number());
        //TODO: check if user is null
        //User currentPrincipal = (User) authentication.getPrincipal();
        //accountManagementForm.setUserId();
        model.addAttribute("accountManagementForm", accountManagementForm);

        return "account-management";
    }

    
}
