    public TransactionResponse<String> invalidateToken() {
        tokenValidator.validateEmptyToken(SecurityContextHolder.getContext().getAuthentication().getCredentials());
        logger.info(" Invalidate Token - Service");
        EPayPrincipal ePayPrincipal = EPayIdentityUtil.getUserPrincipal();
        TokenDto tokenDto = tokenDao.getActiveTokenByMID(ePayPrincipal.getMid(), ePayPrincipal.getToken(),TokenStatus.ACTIVE).orElseThrow(() -> new TransactionException(ErrorConstants.NOT_FOUND_ERROR_CODE, MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "Active Token")));
        buildTokenDtoForInvalidate(tokenDto);
        tokenDao.saveToken(tokenDto);
        return TransactionResponse.<String>builder().data(List.of("Token invalidated successfully")).status(1).build();
    }
