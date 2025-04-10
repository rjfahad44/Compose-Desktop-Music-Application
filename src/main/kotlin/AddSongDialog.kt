import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.awt.FileDialog
import java.awt.Frame

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddSongDialog(onDismiss: () -> Unit, onAddSong: (String, String, String, String) -> Unit) {

    var showErrorDialog by remember { mutableStateOf(false) } // For error alert

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = true,
            usePlatformInsets = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            elevation = 8.dp, // Shadow for popup effect
            modifier = Modifier
                .width(500.dp) // Fixed width like a popup
                .wrapContentHeight() // Height adjusts to content
                .padding(16.dp) // Outer padding from window edges
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add New Song", color = Color.White, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close add dialog", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                var title by remember { mutableStateOf("") }
                var artist by remember { mutableStateOf("") }
                var image by remember { mutableStateOf("") }
                var url by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Image selection with file picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = image,
                        onValueChange = { image = it },
                        label = { Text("Image Path/Url", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) },
                        modifier = Modifier.weight(1f),
                        enabled = false, // Make it read-only
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            disabledTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            disabledBorderColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val fileDialog = FileDialog(null as Frame?, "Select Thumbnail", FileDialog.LOAD)
                            fileDialog.isMultipleMode = false
                            fileDialog.file = "*.jpg;*.png" // Filter for image files
                            fileDialog.isVisible = true
                            val selectedFile = fileDialog.files.firstOrNull()
                            if (selectedFile != null) {
                                image = selectedFile.absolutePath // Update with selected file path
                            }
                            if (image.isEmpty()){
                                image = "images/ic_song.webp" // set default image
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00B0FF)),
                        modifier = Modifier.height(60.dp).padding(top = 6.dp)
                    ) {
                        Text("Browse", color = Color.White, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Track URL", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF3B30)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && artist.isNotBlank() && url.isNotBlank()) {
                                onAddSong(title, artist, image, url)
                            } else {
                                showErrorDialog = true // Show error dialog
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3E9F41)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done", color = Color.White, style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp))
                    }
                }
            }
        }

        // Error Dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Error", color = Color.White) },
                text = { Text("Please fill all the fields", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = { showErrorDialog = false },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBB86FC))
                    ) {
                        Text("OK", color = Color.White)
                    }
                },
                backgroundColor = Color(0xFF1E1E1E),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}