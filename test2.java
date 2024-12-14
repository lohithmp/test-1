@Test
void testInvalidateToken_Success() {
    // Arrange
    when(authentication.getCredentials()).thenReturn("mock-token");
    tokenValidator.validateEmptyToken("mock-token");

    mockPrincipal = new EPayPrincipal("mock-mid", "mock-token");
    when(EPayIdentityUtil.getUserPrincipal()).thenReturn(mockPrincipal);

    TokenDto mockTokenDto = new TokenDto();
    when(tokenDao.getActiveTokenByMID("mock-mid", "mock-token", TokenStatus.ACTIVE))
            .thenReturn(Optional.of(mockTokenDto));

    when(tokenDao.saveToken(any(TokenDto.class))).thenReturn(mockTokenDto); // Mock saveToken behavior

    // Act
    TransactionResponse<String> response = tokenService.invalidateToken();

    // Assert
    assertNotNull(response);
    assertEquals(1, response.getStatus());
    assertEquals(List.of("Token invalidated successfully"), response.getData());

    // Verify interactions
    verify(tokenValidator).validateEmptyToken("mock-token");
    verify(tokenDao).getActiveTokenByMID("mock-mid", "mock-token", TokenStatus.ACTIVE);
    verify(tokenDao).saveToken(mockTokenDto); // Ensure saveToken is invoked
}
