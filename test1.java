    public TransactionResponse<String> invalidateToken() {
        tokenValidator.validateEmptyToken(SecurityContextHolder.getContext().getAuthentication().getCredentials());
        logger.info(" Invalidate Token - Service");
        EPayPrincipal ePayPrincipal = EPayIdentityUtil.getUserPrincipal();
        TokenDto tokenDto = tokenDao.getActiveTokenByMID(ePayPrincipal.getMid(), ePayPrincipal.getToken(),TokenStatus.ACTIVE).orElseThrow(() -> new TransactionException(ErrorConstants.NOT_FOUND_ERROR_CODE, MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "Active Token")));
        buildTokenDtoForInvalidate(tokenDto);
        tokenDao.saveToken(tokenDto);
        return TransactionResponse.<String>builder().data(List.of("Token invalidated successfully")).status(1).build();
    }



 @Test
    void testInvalidateToken() {
        try (MockedStatic<EPayIdentityUtil> mocked = Mockito.mockStatic(EPayIdentityUtil.class, Mockito.CALLS_REAL_METHODS)) {
            EPayPrincipal ePayPrincipal = new EPayPrincipal();
            ePayPrincipal.setMid("Mid_34555");
            ePayPrincipal.setTokenType("==efmmc2342dvxckvxjvin");



            mocked.when(EPayIdentityUtil::getUserPrincipal).thenReturn(ePayPrincipal);
            doNothing().when(validator).validateEmptyToken(any());
            when(tokenDao.getActiveTokenByMID(buildTokenDto().getMerchantId(), ePayPrincipal.getToken(), TokenStatus.ACTIVE)).thenReturn(Optional.ofNullable(buildTokenDto()));
            when(tokenDao.saveToken(any(TokenDto.class))).thenAnswer(invocationOnMock -> {
                TokenDto tokenDto1 = invocationOnMock.getArgument(0);
                tokenDto1.setGeneratedToken("==efmmc2342dvxckvxjvin");
                tokenDto1.setTokenType(TokenType.TRANSACTION);
                tokenDto1.setId(UUID.fromString("462c0c15-6c6c-4061-b2f2-852fa070816a"));
                tokenDto1.setStatus(TokenStatus.ACTIVE);
                return tokenDto1;
            });

            TransactionResponse<String> invalidTokenResponse = tokenService.invalidateToken();

            verify(tokenDao, times(1)).getActiveTokenByMID(buildTokenDto().getMerchantId(), ePayPrincipal.getToken(), TokenStatus.ACTIVE);
            verify(tokenDao, times(1)).saveToken(any(TokenDto.class));

            assertNotNull(invalidTokenResponse);
            assertEquals(1, invalidTokenResponse.getStatus());
            assertEquals(1, invalidTokenResponse.getData().size());
        }
    }

    @Test
    void testInvalidateTokenFailure() {
        try (MockedStatic<EPayIdentityUtil> mocked = Mockito.mockStatic(EPayIdentityUtil.class, Mockito.CALLS_REAL_METHODS)) {
            EPayPrincipal ePayPrincipal = new EPayPrincipal();
            ePayPrincipal.setMid("Mid_34555");
            ePayPrincipal.setTokenType("==efmmc2342dvxckvxjvin");

            mocked.when(EPayIdentityUtil::getUserPrincipal).thenReturn(ePayPrincipal);
            doThrow(new TransactionException(ErrorConstants.NOT_FOUND_ERROR_CODE, MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "Active User")))
                    .when(tokenDao).getActiveTokenByMID(ePayPrincipal.getMid(), ePayPrincipal.getToken(), TokenStatus.ACTIVE);

            TransactionException validationException = assertThrows(TransactionException.class, () -> tokenService.invalidateToken());

            verify(tokenDao, times(1)).getActiveTokenByMID(ePayPrincipal.getMid(), ePayPrincipal.getToken(), TokenStatus.ACTIVE);
            verify(tokenDao, times(0)).saveToken(any(TokenDto.class));

            assertNotNull(validationException);
            assertEquals("1003", validationException.getErrorCode());
            assertEquals("Active User is not found.", validationException.getErrorMessage());
        }

    }







Cannot invoke "org.springframework.security.core.Authentication.getCredentials()" because the return value of "org.springframework.security.core.context.SecurityContext.getAuthentication()" is null
java.lang.NullPointerException: Cannot invoke "org.springframework.security.core.Authentication.getCredentials()" because the return value of "org.springframework.security.core.context.SecurityContext.getAuthentication()" is null
	at com.epay.transaction.service.TokenService.invalidateToken(TokenService.java:103)
	at com.epay.transaction.service.TokenServiceTest.testInvalidateToken(TokenServiceTest.java:145)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

