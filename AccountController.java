package grass.micro.apps.auth.web.controller;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import grass.micro.apps.annotation.GetBody;
import grass.micro.apps.annotation.Logged;
import grass.micro.apps.auth.web.form.AccountForm;
import grass.micro.apps.auth.web.util.DtoFetchingUtils;
import grass.micro.apps.model.auth.Account;
import grass.micro.apps.service.auth.AccountService;
import grass.micro.apps.util.SystemUtils;
import grass.micro.apps.web.component.ErrorsKeyConverter;
import grass.micro.apps.web.controller.GeneralController;
import grass.micro.apps.web.controller.support.AppControllerCreationSupport;
import grass.micro.apps.web.controller.support.AppControllerListingSupport;
import grass.micro.apps.web.controller.support.AppControllerSupport;
import grass.micro.apps.web.dto.RpcResponse;
import grass.micro.apps.web.form.validator.LimittedForm;
import grass.micro.apps.web.util.RequestUtils;

@RestController
public class AccountController extends GeneralController {
	private static final String APP_ACCOUNT_ACTION = "/accounts";
	private static final String APP_ACCOUNT_DETAIL_ACTION = "/accounts/{id}";

	@Autowired
	private AccountService accountService;

	@Autowired
	private ErrorsKeyConverter errorsProcessor;

	@GetMapping(value = APP_ACCOUNT_ACTION)
	@RequiresPermissions(value = "account_view")
	@Logged
	public ResponseEntity<?> listAccount(HttpServletRequest request, HttpServletResponse response,
			@GetBody LimittedForm form, BindingResult errors) {
		
		AppControllerSupport support = new AppControllerListingSupport() {
			
			@Override
			public List<? extends Serializable> getEntityList(HttpServletRequest request, HttpServletResponse response,
					Errors errors, ErrorsKeyConverter errorsProcessor) {
				return AccountController.this.accountService.getAllAvailable();
			}

			@Override
			public String getAttributeName() {
				return "accounts";
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<?> fetchEntitiesToDtos(List<? extends Serializable> entities) {
				return DtoFetchingUtils.fetchAccounts((List<Account>) entities);
			}
		};

		return support.doSupport(request, response, null, errorsProcessor);
	}

	@GetMapping(value = APP_ACCOUNT_DETAIL_ACTION)
	@RequiresPermissions(value = "account_view")
	@Logged
	public ResponseEntity<?> getAccountDetail(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Integer id) {

		AppControllerSupport support = new AppControllerSupport() {
			
			@Override
			public void process(HttpServletRequest request, HttpServletResponse response, Errors errors, ErrorsKeyConverter errorsProccessor) {
				Account account = AccountController.this.accountService.get(id);
				getRpcResponse().addAttribute("account", DtoFetchingUtils.fetchAccount(account));
			}
		};
		return support.doSupport(request, response, RequestUtils.getInstance().getBindingResult(), errorsProcessor);
	}
	
	@PostMapping(value = APP_ACCOUNT_ACTION)
	@Logged
	public ResponseEntity<?> createNewAccount(HttpServletRequest request, @RequestBody AccountForm form, BindingResult errors) {
		AppControllerSupport support = new AppControllerCreationSupport() {
			
			@Override
			public void process(HttpServletRequest request, HttpServletResponse response, Errors errors,
					ErrorsKeyConverter errorsProcessor) {
				String rawPassword = SystemUtils.getInstance().randomPassword();
//				Account account = AccountController.this.doCreateAccount(form, rawPassword, getRpcResponse(),(BindingResult) errors); 
				
			}
		};
		return support.doSupport(request, null, errors, errorsProcessor);
	}

	protected Account doCreateAccount(AccountForm form, String rawPassword, RpcResponse rpcResponse,
			BindingResult errors) {
		Account account = new Account();
		account.setAccountType(form.getAccountType());
		account.setEmail(form.getEmail());
		account.setLoginName(form.getLoginName());
		String salt = SystemUtils.getInstance().randomPassword() + SystemUtils.getInstance().randomPassword();
		account.setPassword(SystemUtils.getInstance().generatePassword(rawPassword, salt));
		return null;
	}

}
