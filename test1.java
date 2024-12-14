import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.MessageFormat;
import java.util.Optional;

class TokenServiceTest {

    @Mock
    private TokenDao tokenDao;

    @Mock
    private TokenValidator tokenValidator;

    @Mock
    private EPayIdentityUtil ePayIdentityUtil;

    @InjectMocks
    private TokenService tokenService; // Replace TokenService with the class where this method is implemented.

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private EPayPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up SecurityContext mock
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testInvalidateToken_Success() {
        // Arrange
        when(authentication.getCredentials()).thenReturn("mock-token");
        doNothing().when(tokenValidator).validateEmptyToken("mock-token");

        mockPrincipal = new EPayPrincipal("mock-mid", "mock-token");
        when(EPayIdentityUtil.getUserPrincipal()).thenReturn(mockPrincipal);

        TokenDto mockTokenDto = new TokenDto();
        when(tokenDao.getActiveTokenByMID("mock-mid", "mock-token", TokenStatus.ACTIVE))
                .thenReturn(Optional.of(mockTokenDto));

        doNothing().when(tokenDao).saveToken(any(TokenDto.class));

        // Act
        TransactionResponse<String> response = tokenService.invalidateToken();

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getStatus());
        assertEquals(List.of("Token invalidated successfully"), response.getData());

        // Verify interactions
        verify(tokenValidator).validateEmptyToken("mock-token");
        verify(tokenDao).getActiveTokenByMID("mock-mid", "mock-token", TokenStatus.ACTIVE);
        verify(tokenDao).saveToken(mockTokenDto);
    }

    @Test
    void testInvalidateToken_TokenNotFound() {
        // Arrange
        when(authentication.getCredentials()).thenReturn("mock-token");
        doNothing().when(tokenValidator).validateEmptyToken("mock-token");

        mockPrincipal = new EPayPrincipal("mock-mid", "mock-token");
        when(EPayIdentityUtil.getUserPrincipal()).thenReturn(mockPrincipal);

        when(tokenDao.getActiveTokenByMID("mock-mid", "mock-token", TokenStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        TransactionException exception = assertThrows(TransactionException.class, () -> {
            tokenService.invalidateToken();
        });

        assertEquals(ErrorConstants.NOT_FOUND_ERROR_CODE, exception.getErrorCode());
        assertEquals(
                MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "Active Token"),
                exception.getErrorMessage()
        );

        // Verify interactions
        verify(tokenValidator).validateEmptyToken("mock-token");
        verify(tokenDao).getActiveTokenByMID("mock-mid", "mock-token", TokenStatus.ACTIVE);
        verifyNoMoreInteractions(tokenDao);
    }
}
