
Only void methods can doNothing()!
Example of correct use of doNothing():
    doNothing().
    doThrow(new RuntimeException())
    .when(mock).someVoidMethod();
Above means:
someVoidMethod() does nothing the 1st time but throws an exception the 2nd time is called
org.mockito.exceptions.base.MockitoException: 
Only void methods can doNothing()!
Example of correct use of doNothing():
    doNothing().
    doThrow(new RuntimeException())
    .when(mock).someVoidMethod();
Above means:
someVoidMethod() does nothing the 1st time but throws an exception the 2nd time is called
	at com.epay.transaction.dao.TokenDao.saveToken(TokenDao.java:55)
	at com.epay.transaction.service.TokenServiceTest.testInvalidateToken_Success(TokenServiceTest.java:151)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
