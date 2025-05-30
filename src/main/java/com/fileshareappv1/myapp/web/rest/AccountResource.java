package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.security.SecurityUtils;
import com.fileshareappv1.myapp.service.FileService;
import com.fileshareappv1.myapp.service.MailService;
import com.fileshareappv1.myapp.service.UserService;
import com.fileshareappv1.myapp.service.dto.AdminUserDTO;
import com.fileshareappv1.myapp.service.dto.FileDTO;
import com.fileshareappv1.myapp.service.dto.PasswordChangeDTO;
import com.fileshareappv1.myapp.service.dto.UserDTO;
import com.fileshareappv1.myapp.service.storage.LocalStorageService;
import com.fileshareappv1.myapp.web.rest.errors.*;
import com.fileshareappv1.myapp.web.rest.vm.KeyAndPasswordVM;
import com.fileshareappv1.myapp.web.rest.vm.ManagedUserVM;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;
    private final LocalStorageService localStorageService;
    private final FileService fileService;
    private final LocalStorageService storageService;

    public AccountResource(
        UserRepository userRepository,
        UserService userService,
        MailService mailService,
        LocalStorageService localStorageService,
        FileService fileService,
        LocalStorageService storageService
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.localStorageService = localStorageService;
        this.fileService = fileService;
        this.storageService = storageService;
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already used.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        return userService
            .getUserWithAuthorities()
            .map(AdminUserDTO::new)
            .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    //    /**
    //     * {@code POST  /account} : update the current user information.
    //     *
    //     * @param userDTO the current user information.
    //     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
    //     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user login wasn't found.
    //     */
    //    @PostMapping("/account")
    //    public Map<String, Object> saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
    //        String userLogin = SecurityUtils.getCurrentUserLogin()
    //            .orElseThrow(() -> new AccountResourceException("Current user login not found"));
    //        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
    //        if (existingUser.isPresent() && (!existingUser.orElseThrow().getLogin().equalsIgnoreCase(userLogin))) {
    //            throw new EmailAlreadyUsedException();
    //        }
    //        Optional<User> user = userRepository.findOneByLogin(userLogin);
    //        if (!user.isPresent()) {
    //            throw new AccountResourceException("User could not be found");
    //        }
    //        return userService.updateUser(
    //            userDTO.getFirstName(),
    //            userDTO.getLastName(),
    //            userDTO.getEmail(),
    //            userDTO.getLangKey(),
    //            userDTO.getImageUrl(),
    //            userDTO.getPhoneNumber(),
    //            userDTO.getAddress(),
    //            userDTO.getDateOfBirth()
    //        );
    //    }

    @PostMapping(value = "/account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminUserDTO> updateAccount(
        @ModelAttribute AdminUserDTO form,
        @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        // 1) lookup the current user
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Not authenticated", "account", "notloggedin"));
        User user = userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> new BadRequestAlertException("User not found: " + login, "account", "usernotfound"));

        if (file != null && !file.isEmpty()) {
            String stored = localStorageService.store(file);
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/files/download/").path(stored).toUriString();

            FileDTO fd = new FileDTO();
            fd.setFileName(stored);
            fd.setFileUrl(fileUrl);
            fd.setMimeType(Optional.ofNullable(file.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
            fd.setFileSize(file.getSize());
            fd.setUploadedAt(Instant.now());
            fileService.save(fd);

            user.setImageUrl(fileUrl);
        }

        // 3) update the rest of the profile
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setEmail(form.getEmail().toLowerCase());
        if (form.getLangKey() != null) user.setLangKey(form.getLangKey());
        if (form.getPhoneNumber() != null) user.setPhoneNumber(form.getPhoneNumber());
        if (form.getAddress() != null) user.setAddress(form.getAddress());
        if (form.getDateOfBirth() != null) user.setDateOfBirth(form.getDateOfBirth());

        user.setLastModifiedBy(login);
        user.setLastModifiedDate(Instant.now());
        userRepository.save(user);

        // 4) build and return AdminUserDTO
        AdminUserDTO out = new AdminUserDTO(user);
        return ResponseEntity.ok(out);
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String mail) {
        Optional<User> user = userService.requestPasswordReset(mail);
        if (user.isPresent()) {
            mailService.sendPasswordResetMail(user.orElseThrow());
        } else {
            // Pretend the request has been successful to prevent checking which emails really exist
            // but log that an invalid attempt has been made
            LOG.warn("Password reset requested for non existing mail");
        }
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (
            StringUtils.isEmpty(password) ||
            password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
            password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH
        );
    }

    @PostMapping(value = "/account/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminUserDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestAlertException("No file provided", "account", "filenotprovided");
        }

        String storedFilename = localStorageService.store(file);

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/files/download/")
            .path(storedFilename)
            .toUriString();

        FileDTO f = new FileDTO();
        f.setFileName(storedFilename);
        f.setFileUrl(fileUrl);
        f.setMimeType(Optional.ofNullable(file.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        f.setFileSize(file.getSize());
        f.setUploadedAt(Instant.now());
        fileService.save(f);

        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Not authenticated", "account", "notloggedin"));
        User user = userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> new BadRequestAlertException("User not found: " + login, "account", "usernotfound"));

        user.setImageUrl(fileUrl);
        user.setLastModifiedBy(login);
        user.setLastModifiedDate(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(new AdminUserDTO(user));
    }
}
