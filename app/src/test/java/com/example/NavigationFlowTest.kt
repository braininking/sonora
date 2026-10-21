package com.example

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.model.Song
import com.example.ui.NavigationTab
import com.example.ui.SonoraViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NavigationFlowTest {

    private lateinit var app: SonoraApplication
    private lateinit var viewModel: SonoraViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = SonoraViewModel(app)
    }

    @Test
    fun testSpecificNavigationProtocol() {
        val testSong = Song(
            id = 101L,
            title = "Ode to Joy",
            artist = "Beethoven",
            album = "Symphony No. 9",
            albumId = 202L,
            durationMs = 240000L,
            contentUri = Uri.parse("content://media/external/audio/media/101"),
            filePath = "/storage/emulated/0/Music/beethoven.mp3",
            folderPath = "/storage/emulated/0/Music",
            folderName = "Music",
            genre = "Classical",
            year = 1824,
            trackNumber = 4,
            sizeBytes = 5000000L,
            dateAdded = 1700000000L
        )

        // 1. Iniciar uma música
        viewModel.playSong(testSong, listOf(testSong))
        assertNotNull(viewModel.playerController.currentSong.value)

        // 2. Abrir o player completo
        viewModel.setFullPlayerVisible(true)
        assertTrue(viewModel.isFullPlayerVisible.value)

        // 3. Tocar em "Pastas" na barra inferior
        viewModel.setTab(NavigationTab.FOLDERS)

        // 4. Verificar se a tela de Pastas abre
        assertEquals(NavigationTab.FOLDERS, viewModel.currentTab.value)
        assertFalse("Full player must be closed when tapping bottom bar tab", viewModel.isFullPlayerVisible.value)

        // 5. Verificar se a música continua tocando
        assertNotNull(viewModel.playerController.currentSong.value)

        // 6. Verificar se o mini-player aparece (currentSong != null && !isFullPlayerVisible)
        val isMiniPlayerVisible = viewModel.playerController.currentSong.value != null && !viewModel.isFullPlayerVisible.value
        assertTrue("Mini-player must be visible when player is dismissed and song is playing", isMiniPlayerVisible)

        // 7. Tocar em "Álbuns"
        viewModel.setTab(NavigationTab.ALBUMS)
        assertEquals(NavigationTab.ALBUMS, viewModel.currentTab.value)
        assertFalse(viewModel.isFullPlayerVisible.value)

        // 8. Tocar em "Artistas"
        viewModel.setTab(NavigationTab.ARTISTS)
        assertEquals(NavigationTab.ARTISTS, viewModel.currentTab.value)
        assertFalse(viewModel.isFullPlayerVisible.value)

        // 9. Tocar em "Músicas"
        viewModel.setTab(NavigationTab.SONGS)
        assertEquals(NavigationTab.SONGS, viewModel.currentTab.value)
        assertFalse(viewModel.isFullPlayerVisible.value)

        // 10. Tocar em "Início"
        viewModel.setTab(NavigationTab.HOME)
        assertEquals(NavigationTab.HOME, viewModel.currentTab.value)
        assertFalse(viewModel.isFullPlayerVisible.value)

        // 11. Voltar ao player pelo mini-player
        viewModel.setFullPlayerVisible(true)
        assertTrue("Full player must open when expanding from mini-player", viewModel.isFullPlayerVisible.value)
        assertEquals("Song must continue uninterrupted", testSong.id, viewModel.playerController.currentSong.value?.id)
    }
}
