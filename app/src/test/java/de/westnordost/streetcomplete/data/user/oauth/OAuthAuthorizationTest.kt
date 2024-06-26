package de.westnordost.streetcomplete.data.user.oauth

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.http.ParametersBuilder
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OAuthAuthorizationTest {
    @Test fun createAuthorizationUrl() {
        val url = Url(createOAuth().authorizationRequestUrl)

        assertEquals("https", url.protocol.name)
        assertEquals("test.me", url.host)
        assertEquals("/auth", url.encodedPath)

        assertEquals("code", url.parameters["response_type"])
        assertEquals("ClientId %#+!", url.parameters["client_id"])
        assertEquals("localhost://oauth", url.parameters["redirect_uri"])
        assertEquals("one! 2 THREE+(1/2)", url.parameters["scope"])
        assertEquals("S256", url.parameters["code_challenge_method"])
        assertTrue(url.parameters["code_challenge"]!!.length <= 128)
        assertTrue(url.parameters["code_challenge"]!!.length >= 43)
    }

    @Test fun `createAuthorizationUrl with state`() {
        val parameters = Url(createOAuth("123").authorizationRequestUrl).parameters
        assertEquals("123", parameters["state"])
    }

    @Test fun `generates different code challenge for each instance`() {
        val url1 = Url(createOAuth().authorizationRequestUrl)
        val url2 = Url(createOAuth().authorizationRequestUrl)
        assertTrue(url1.parameters["code_challenge"] != url2.parameters["code_challenge"])
    }

    @Test fun `serializes correctly`() {
        val oauth1 = createOAuth()
        val oauth1String = Json.encodeToString(oauth1)
        val oauth2 = Json.decodeFromString<OAuthAuthorizationParams>(oauth1String)
        val oauth2String = Json.encodeToString(oauth2)

        assertEquals(oauth1String, oauth2String)
    }

    @Test fun `itsForMe with state`() {
        val state = "123"
        val oauth = createOAuth(state)

        assertFalse(oauth.itsForMe("This isn't::::a valid URL"))
        assertFalse(oauth.itsForMe("localhost://oauth")) // no state
        assertFalse(oauth.itsForMe("localhost://oauth?state=abc")) // different state
        assertTrue(oauth.itsForMe("localhost://oauth?state=$state")) // same state
        // different uri
        assertFalse(oauth.itsForMe("localhost://oauth3?state=$state"))
        assertFalse(oauth.itsForMe("localhost://oauth/path?state=$state"))
        assertFalse(oauth.itsForMe("localboost://oauth?state=$state"))
    }

    @Test fun `itsForMe without state`() {
        val oauth = createOAuth()

        assertTrue(oauth.itsForMe("localhost://oauth")) // no state
        assertFalse(oauth.itsForMe("localhost://oauth?state=abc")) // different state
        // different uri
        assertFalse(oauth.itsForMe("localhost://oauth3"))
        assertFalse(oauth.itsForMe("localhost://oauth/path"))
        assertFalse(oauth.itsForMe("localboost://oauth"))
    }

    @Test fun `extractAuthorizationCode fails with useful error messages`(): Unit = runBlocking {
        val oauth = createOAuth()
        val service = OAuthService(HttpClient(MockEngine { respondOk() }))

        // server did not respond correctly with "error"
        assertFailsWith<OAuthConnectionException> {
            service.retrieveAccessToken(oauth, "localhost://oauth?e=something")
        }

        try {
            service.retrieveAccessToken(oauth, "localhost://oauth?error=hey%2Bwhat%27s%2Bup")
        } catch (e: OAuthException) {
            assertEquals("hey what's up", e.message)
        }

        try {
            service.retrieveAccessToken(oauth, "localhost://oauth?error=A%21&error_description=B%21")
        } catch (e: OAuthException) {
            assertEquals("A!: B!", e.message)
        }

        try {
            service.retrieveAccessToken(oauth, "localhost://oauth?error=A%21&error_uri=http%3A%2F%2Fabc.de")
        } catch (e: OAuthException) {
            assertEquals("A! (see http://abc.de)", e.message)
        }

        try {
            service.retrieveAccessToken(oauth, "localhost://oauth?error=A%21&error_description=B%21&error_uri=http%3A%2F%2Fabc.de")
        } catch (e: OAuthException) {
            assertEquals("A!: B! (see http://abc.de)", e.message)
        }
    }

    @Test fun extractAuthorizationCode() = runBlocking {
        val service = OAuthService(HttpClient(MockEngine { request ->
            if (request.url.parameters["code"] == "my code") {
                respondOk("""{
                    "access_token": "TOKEN",
                    "token_type": "bearer",
                    "scope": "A B C"
                }""")
            } else {
                respondError(HttpStatusCode.BadRequest)
            }
        }))
        val oauth = createOAuth()

        assertEquals(
            AccessTokenResponse("TOKEN", listOf("A", "B", "C")),
            service.retrieveAccessToken(oauth, "localhost://oauth?code=my%20code")
        )
    }

    @Test fun `retrieveAccessToken throws OAuthConnectionException with invalid response token_type`(): Unit = runBlocking {
        val service = OAuthService(HttpClient(MockEngine { respondOk("""{
            "access_token": "TOKEN",
            "token_type": "an_unusual_token_type",
            "scope": "A B C"
        }""")
        }))

        val exception = assertFailsWith<OAuthConnectionException> {
            service.retrieveAccessToken(dummyOAuthAuthorization(), "localhost://oauth?code=code")
        }

        assertEquals(
            "OAuth 2 token endpoint returned an unknown token type (an_unusual_token_type)",
            exception.message
        )
    }

    @Test fun `retrieveAccessToken throws OAuthException when error response`(): Unit = runBlocking {
        val service = OAuthService(HttpClient(MockEngine { respondError(
            HttpStatusCode.BadRequest, """{
                "error": "Missing auth code",
                "error_description": "Please specify a code",
                "error_uri": "code"
            }"""
        ) }))

        val exception = assertFailsWith<OAuthException> {
            service.retrieveAccessToken(dummyOAuthAuthorization(), "localhost://oauth?code=code")
        }

        assertEquals("Missing auth code", exception.error)
        assertEquals("Please specify a code", exception.description)
        assertEquals("code", exception.uri)
        assertEquals("Missing auth code: Please specify a code (see code)", exception.message)
    }

    @Test fun `retrieveAccessToken generates correct request URL`(): Unit = runBlocking {
        val mockEngine = MockEngine { respondOk() }
        val auth = OAuthAuthorizationParams(
            "",
            "https://www.openstreetmap.org",
            "OAuthClientId",
            listOf(),
            "scheme://there"
        )

        assertFails { OAuthService(HttpClient(mockEngine)).retrieveAccessToken(auth, "scheme://there?code=C0D3") }

        val expectedParams = ParametersBuilder()
        expectedParams.append("grant_type", "authorization_code")
        expectedParams.append("client_id", "OAuthClientId")
        expectedParams.append("code", "C0D3")
        expectedParams.append("redirect_uri", "scheme://there")
        expectedParams.append("code_verifier", auth.codeVerifier)

        assertEquals(1, mockEngine.requestHistory.size)
        assertEquals(expectedParams.build(), mockEngine.requestHistory[0].url.parameters)
        assertEquals("www.openstreetmap.org", mockEngine.requestHistory[0].url.host)
    }

    @Test fun `retrieveAccessToken generates request headers`(): Unit = runBlocking {
        val mockEngine = MockEngine { respondOk() }

        assertFails {
            OAuthService(HttpClient(mockEngine)).retrieveAccessToken(dummyOAuthAuthorization(), "localhost://oauth?code=code")
        }

        val expectedHeaders = HeadersBuilder()
        expectedHeaders.append("Content-Type", "application/x-www-form-urlencoded")
        expectedHeaders.append("Accept-Charset", "UTF-8")
        expectedHeaders.append("Accept", "*/*")

        assertEquals(1, mockEngine.requestHistory.size)
        assertEquals(expectedHeaders.build(), mockEngine.requestHistory[0].headers)
    }
}

private fun dummyOAuthAuthorization() = OAuthAuthorizationParams("", "", "", listOf(), "")

private fun createOAuth(state: String? = null) = OAuthAuthorizationParams(
    "https://test.me/auth",
    "https://test.me/token",
    "ClientId %#+!",
    listOf("one!", "2", "THREE+(1/2)"),
    "localhost://oauth",
    state
)
