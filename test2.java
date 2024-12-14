import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private TokenValidator tokenValidator;

    @Mock
    private TokenDao tokenDao;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private EPayPrincipal ePayPrincipal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInvalidateToken_Success() {
        // Mock SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn("test-token");
        SecurityContextHolder.setContext(securityContext);

        // Mock EPayPrincipal
        when(EPayIdentityUtil.getUserPrincipal()).thenReturn(ePayPrincipal);
        when(ePayPrincipal.getMid()).thenReturn("test-mid");
        when(ePayPrincipal.getToken()).thenReturn("test-token");

        // Mock TokenDao
        TokenDto mockTokenDto = new TokenDto();
        when(tokenDao.getActiveTokenByMID("test-mid", "test-token", TokenStatus.ACTIVE))
                .thenReturn(Optional.of(mockTokenDto));

        // Execute the method
        TransactionResponse<String> response = tokenService.invalidateToken();

        // Verify interactions and response
        verify(tokenValidator).validateEmptyToken("test-token");
        verify(tokenDao).saveToken(mockTokenDto);
        assertNotNull(response);
        assertEquals(1, response.getStatus());
        assertEquals("Token invalidated successfully", response.getData().get(0));
    }

    @Test
    void testInvalidateToken_TokenNotFound() {
        // Mock SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getCredentials()).thenReturn("test-token");
        SecurityContextHolder.setContext(securityContext);

        // Mock EPayPrincipal
        when(EPayIdentityUtil.getUserPrincipal()).thenReturn(ePayPrincipal);
        when(ePayPrincipal.getMid()).thenReturn("test-mid");
        when(ePayPrincipal.getToken()).thenReturn("test-token");

        // Mock TokenDao
        when(tokenDao.getActiveTokenByMID("test-mid", "test-token", TokenStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Assert exception
        TransactionException exception = assertThrows(TransactionException.class, () -> tokenService.invalidateToken());
        assertEquals(ErrorConstants.NOT_FOUND_ERROR_CODE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Active Token"));

        // Verify interactions
        verify(tokenValidator).validateEmptyToken("test-token");
        verify(tokenDao, never()).saveToken(any());
    }
}
