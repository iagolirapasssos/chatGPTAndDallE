/*
 * ChatGPT
 * Extension to use OpenAI's ChatGPT API
 * Version: 1.0
 * Author: Francisco Iago Lira Passos
 * Date: 2023-10-15
 */

package com.bosonshiggs.chatgpt;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.common.OptionList;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.annotations.SimpleEvent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.util.Map;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.bosonshiggs.chatgpt.helpers.DallEModel;
import com.bosonshiggs.chatgpt.helpers.ImageSize;
import com.bosonshiggs.chatgpt.helpers.VoiceModel;
import com.bosonshiggs.chatgpt.helpers.AudioQuality;
import com.bosonshiggs.chatgpt.helpers.AudioFormats;

import android.os.Environment;
import android.media.MediaScannerConnection;

import android.util.Log;

//Auxiliar methods Dialogs
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.graphics.drawable.ColorDrawable;
import android.content.DialogInterface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import android.media.MediaPlayer;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.SeekBar;
import android.os.Handler;


@DesignerComponent(version = 7, // Update version here, You must do for each new release to upgrade your extension
description = "Extension to use Openai's API",
category = ComponentCategory.EXTENSION,
nonVisible = true,
iconName = "images/extension.png") // Change your extension's icon from here; can be a direct url


@SimpleObject(external = true)
public class ChatGPT extends AndroidNonvisibleComponent {
	private String LOG_NAME = "ChatGPT";
    private boolean flagLog = false;
    
    private ComponentContainer container;
    
    public ChatGPT(ComponentContainer container) {
        super(container.$form());
        this.container = container;
    }
    
    
    @SimpleFunction(description = "Extract Value from JSON Text")
    public String jsonDecode(final String jsonText, final String key) {
        try {
            // Converter o JSON em um objeto JSON
            JSONObject jsonObject = new JSONObject(jsonText);

            // Use a função auxiliar para buscar o valor da chave no objeto JSON
            String value = findValueForKey(jsonObject, key);

            if (value != null) {
                return value;
            } else {
                return "Error! Key not found!";
            }
        } catch (JSONException e) {
            ReportError(e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    @SimpleFunction(description = "Send a prompt to ChatGPT")
    public void SendPrompt(final String prompt, final String model, final String systemMsg, final float temperature, final String apiKey) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = sendRequest(prompt, model, systemMsg, temperature, apiKey);
                if (responseText != null) {
                    ResponseReceived(responseText);
                } else {
                	ReportError("None");
                }
            }
        }).start();
    }
    
    @SimpleFunction(description = "Generate an image based on a prompt")
    public void GenerateImage(
    		String prompt, 
    		int numberOfImages, 
    		String apiKey,
    		@Options(DallEModel.class) String model,
    		@Options(ImageSize.class) String size
    		) 
    {
    	/*
    	DallEModel model = DallEModel.fromUnderlyingValue(dalleModel);
    	ImageSize size = ImageSize.fromUnderlyingValue(imageSize);
    	*/
    	
    	if (flagLog) Log.i(LOG_NAME, "GenerateImage - model: " + model);
    	if (flagLog) Log.i(LOG_NAME, "GenerateImage - size: " + size);
    	
    	if (model == "dall-e-3") {
    		if (numberOfImages > 1 || numberOfImages < 1) numberOfImages = 1;
    	} else if (model == "dall-e-2") {
    		if (numberOfImages > 10 || numberOfImages < 1) numberOfImages = 1;
    	}

    	final ArrayList<String[]> headHTTP = new ArrayList<>();
    	
    	headHTTP.add(new String[]{"Content-Type", "application/json"});
    	headHTTP.add(new String[]{"Authorization", "Bearer " + apiKey});
    	
    	final String url = "https://api.openai.com/v1/images/generations";
    	
    	final String payload = "{\n"
    		    + "    \"model\": \"" + model + "\",\n"
    		    + "    \"prompt\": \"" + prompt + "\",\n"
    		    + "    \"n\": " + numberOfImages + ",\n"
    		    + "    \"size\": \"" + size + "\"\n"
    		    + "}";
    	
    	if (flagLog) Log.i(LOG_NAME, "GenerateImage - payload: " + payload);
    	if (flagLog) Log.i(LOG_NAME, "GenerateImage - headHTTP: " + headHTTP);
        new Thread(new Runnable() {
            @Override
            public void run() {            	
                String responseText = sendImageGenerationRequest(url, payload, headHTTP);
                if (responseText != null) {
                    ImageGenerationCompleted(responseText);
                } else {
                	ReportError("None");
                }
            }
        }).start();
    }
    
    @SimpleFunction(description = "Create a thread")
    public void CreateThread(final String apiKey) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (flagLog) Log.i(LOG_NAME, "CreateThread - apiKey: " + apiKey);
                
                try {
	                String responseText = sendThreadRequest("POST", "https://api.openai.com/v1/threads", apiKey, "");
	                if (responseText != null) {
	                    ThreadCreated(responseText);
	                } else {
	                    ReportError("None");
	                }
                } catch (Exception e) {
                	if (flagLog) Log.e(LOG_NAME, "Error: " + e.getMessage(), e);
                	ReportError("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    
    
 // Método para recuperar uma thread
    @SimpleFunction(description = "Retrieve a thread")
    public void RetrieveThread(final String apiKey, final String threadId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = sendThreadRequest("GET", "https://api.openai.com/v1/threads/" + threadId, apiKey, "");
                if (responseText != null) {
                    ThreadRetrieved(responseText);
                } else {
                    ReportError("None");
                }
            }
        }).start();
    }
    
 // Método para modificar uma thread
    @SimpleFunction(description = "Modify a thread")
    public void ModifyThread(final String apiKey, final String threadId, final String metadata) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = sendThreadRequest("POST", "https://api.openai.com/v1/threads/" + threadId, apiKey, metadata);
                if (responseText != null) {
                    ThreadModified(responseText);
                } else {
                    ReportError("None");
                }
            }
        }).start();
    }
    
 // Método para excluir uma thread
    @SimpleFunction(description = "Delete a thread")
    public void DeleteThread(final String apiKey, final String threadId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = sendThreadRequest("DELETE", "https://api.openai.com/v1/threads/" + threadId, apiKey, "");
                if (responseText != null) {
                    ThreadDeleted(responseText);
                } else {
                    ReportError("None");
                }
            }
        }).start();
    }
    
 // Método para criar uma mensagem em uma thread
    @SimpleFunction(description = "Create a message in a thread")
    public void CreateMessage(final String apiKey, final String threadId, final String messageContent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String payload = "{\n" +
                                 "    \"role\": \"user\",\n" +
                                 "    \"content\": \"" + messageContent + "\"\n" +
                                 "}";
                String responseText = sendThreadMessageRequest("POST", "https://api.openai.com/v1/threads/" + threadId + "/messages", apiKey, payload);
                if (responseText != null) {
                    MessageCreated(responseText);
                } else {
                    ReportError("None");
                }
            }
        }).start();
    }

    // Método para recuperar uma mensagem em uma thread
    @SimpleFunction(description = "Retrieve a message in a thread")
    public void RetrieveMessage(final String apiKey, final String threadId, final String messageId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = sendThreadMessageRequest("GET", "https://api.openai.com/v1/threads/" + threadId + "/messages/" + messageId, apiKey, "");
                if (responseText != null) {
                    MessageRetrieved(responseText);
                } else {
                    ReportError("None");
                }
            }
        }).start();
    }

    // Método para modificar uma mensagem em uma thread
    @SimpleFunction(description = "Modify a message in a thread")
    public void ModifyMessage(final String apiKey, final String threadId, final String messageId, final String metadata) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = sendThreadMessageRequest("POST", "https://api.openai.com/v1/threads/" + threadId + "/messages/" + messageId, apiKey, metadata);
                if (responseText != null) {
                    MessageModified(responseText);
                } else {
                    ReportError("None");
                }
            }
        }).start();
    }
    

    // Método para listar mensagens em uma thread
    @SimpleFunction(description = "List messages in a thread")
    public void ListMessages(final String apiKey, final String threadId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = sendThreadMessageRequest("GET", "https://api.openai.com/v1/threads/" + threadId + "/messages", apiKey, "");
                if (responseText != null) {
                    MessagesListed(responseText);
                } else {
                    ReportError("None");
                }
            }
        }).start();
    }
    
    /*
     * AUXILIAR METHODS
     */
    
 // Método para mostrar um diálogo de reprodução de áudio
    @SimpleFunction(description = "Show a dialog with a native audio player.")
    public void ShowNativeAudioPlayerDialog(
            String title, 
            final String audioFilePath,
            final int titleColor, 
            final int titleBackgroundColor, 
            final int dialogBackgroundColor) {

        AlertDialog.Builder builder = new AlertDialog.Builder(container.$context());
        builder.setTitle(title);

        // Criar MediaPlayer
        final MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            ReportError("Error preparing MediaPlayer: " + e.getMessage());
            return;
        }

        // Criar layout para adicionar os controles
        LinearLayout layout = new LinearLayout(container.$context());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Barra de tempo
        final SeekBar seekBar = new SeekBar(container.$context());
        seekBar.setMax(mediaPlayer.getDuration());
        layout.addView(seekBar);

        // Atualizar SeekBar com o progresso do MediaPlayer
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 0);

        // Botões de controle
        Button playButton = new Button(container.$context());
        playButton.setText("Play");
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });

        Button pauseButton = new Button(container.$context());
        pauseButton.setText("Pause");
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
            }
        });

        layout.addView(playButton);
        layout.addView(pauseButton);

        builder.setView(layout);

        // Adicionar botão de fechar
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                handler.removeCallbacks(runnable); // Parar atualização da SeekBar
                dialog.dismiss();
            }
        });

        // Criar o AlertDialog
        final AlertDialog dialog = builder.create();

        // Configurar cores
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                TextView titleView = dialog.findViewById(android.R.id.title);
                if (titleView != null) {
                    titleView.setTextColor(titleColor);
                }
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(titleBackgroundColor));
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(dialogBackgroundColor));
        dialog.show();
    }
    
    @SimpleFunction(description = "Shows a dialog with a text input.")
    public void ShowTextInputDialog(
    		String title, 
    		String message, 
    		int textColor, 
    		int editTextBackgroundColor, 
    		final int dialogBackgroundColor, 
    		String hint, 
    		String defaultText, 
    		boolean isFullScreen, 
    		boolean showCancelButton, 
    		boolean showOkButton,
    		final String type
    		) 
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(container.$context());

        // Define o título e a mensagem do diálogo
        builder.setTitle(title);
        builder.setMessage(message);

        // Cria o EditText para entrada de texto
        final EditText input = new EditText(container.$context());
        input.setTextColor(textColor);
        input.setBackgroundColor(editTextBackgroundColor);
        input.setHint(hint);
        input.setText(defaultText);

        // Configuração de layout para o EditText
        if (isFullScreen) {
            builder.setView(input);
        } else {
            int margin = 30; // Margem em pixels (ajuste conforme necessário)
            FrameLayout frameLayout = new FrameLayout(container.$context());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(margin, margin, margin, margin);
            input.setLayoutParams(params);
            frameLayout.addView(input);
            builder.setView(frameLayout);
        }

        // Cria o AlertDialog
        final AlertDialog dialog = builder.create();

        // Adiciona botões ao diálogo
        if (showCancelButton) {
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }

        if (showOkButton) {
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String enteredText = input.getText().toString();
                    TextEntered(enteredText, type);
                }
            });
        }

        // Configurar cor de fundo do diálogo
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(dialogBackgroundColor));
            }
        });

        // Mostrar o diálogo
        dialog.show();
    }
    
    @SimpleFunction(description = "Shows a dialog with a list of items.")
    public void ShowListDialog(
            String title,
            YailList items,
            final int textColor,
            final int listItemBackgroundColor,
            final int dialogBackgroundColor,
            final boolean isFullScreen,
            final boolean showCancelButton,
            final String type
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(container.$context());
        builder.setTitle(title);

        // Converte YailList para array de Strings
        final String[] itemsArray = items.toStringArray();

        // Criar ArrayAdapter personalizado
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.$context(), android.R.layout.simple_list_item_1, itemsArray) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView item = (TextView) super.getView(position, convertView, parent);
                item.setTextColor(textColor); // Define a cor do texto
                item.setBackgroundColor(listItemBackgroundColor); // Define a cor de fundo
                return item;
            }
        };

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = itemsArray[which];
                ListItemSelected(selectedItem, type);
            }
        });

        // Cria o AlertDialog
        final AlertDialog dialog = builder.create();

        // Adiciona botão de cancelamento, se necessário
        if (showCancelButton) {
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }

        // Configurar cor de fundo do diálogo
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if (isFullScreen) {
                    // Aplica a cor de fundo para o modo tela cheia
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(dialogBackgroundColor));
            }
        });

        // Mostrar o diálogo
        dialog.show();
    }
    
    @SimpleFunction(description = "Generate speech from text")
    public void GenerateSpeech(
            final String prompt,
            final String apiKey,
            final String audioName,
            final String directoryName,
            final @Options(VoiceModel.class) String voice,
            final @Options(AudioQuality.class) String model,
            final @Options(AudioFormats.class) String outputFormat
            ) {
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendSpeechRequest(prompt, model, voice, apiKey, audioName, directoryName, outputFormat);
            }
        }).start();
    }
    
    /*
     * EVENTS
     */
    
    @SimpleEvent(description = "Speech file generated successfully")
    public void SpeechGenerated(String filePath) {
        EventDispatcher.dispatchEvent(this, "SpeechGenerated", filePath);
    }
    
    @SimpleEvent(description = "Message created successfully")
    public void MessageCreated(final String messageData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "MessageCreated", messageData);
                }
            });
        }
    }
    
    @SimpleEvent(description = "Message retrieved successfully")
    public void MessageRetrieved(final String messageData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "MessageRetrieved", messageData);
                }
            });
        }
    }

    @SimpleEvent(description = "Message modified successfully")
    public void MessageModified(final String messageData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "MessageModified", messageData);
                }
            });
        }
    }

    @SimpleEvent(description = "Messages listed successfully")
    public void MessagesListed(final String messagesData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "MessagesListed", messagesData);
                }
            });
        }
    }

    @SimpleEvent(description = "Thread deleted successfully")
    public void ThreadDeleted(final String threadData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "ThreadDeleted", threadData);
                }
            });
        }
    }

    @SimpleEvent(description = "Thread modified successfully")
    public void ThreadModified(final String threadData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "ThreadModified", threadData);
                }
            });
        }
    }
    
    @SimpleEvent(description = "Thread retrieved successfully")
    public void ThreadRetrieved(final String threadData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "ThreadRetrieved", threadData);
                }
            });
        }
    }

    @SimpleEvent(description = "Thread created successfully")
    public void ThreadCreated(final String threadData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "ThreadCreated", threadData);
                }
            });
        }
    }
    
    @SimpleEvent(description = "Received a response from ChatGPT")
    public void ResponseReceived(final String response) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "ResponseReceived", response);
                }
            });
        }
    }

    @SimpleEvent(description = "Report an error with a custom message")
    public void ReportError(final String errorMessage) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "ReportError", errorMessage);
                }
            });
        }
    }
    
    @SimpleEvent(description = "Received a generated image")
    public void ImageGenerationCompleted(final String imageData) {
        if (form != null) {
            form.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ChatGPT.this, "ImageGenerationCompleted", imageData);
                }
            });
        }
    }
    
    @SimpleEvent(description = "Triggered when text is entered in the input dialog.")
    public void TextEntered(String text, String type) {
        EventDispatcher.dispatchEvent(this, "TextEntered", text, type);
    }
    
    @SimpleEvent(description = "Triggered when an item is selected from the list dialog.")
    public void ListItemSelected(String item, String type) {
        EventDispatcher.dispatchEvent(this, "ListItemSelected", item, type);
    }
    
    /*
     * PRIVATE METHODS
     */
    
    //Generate speech
    private void sendSpeechRequest(
    		final String text, 
    		final String model, 
    		final String voice, 
    		final String apiKey, 
    		final String audioName, 
    		final String directoryName,
    		final String outputFormat
    		) 
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://api.openai.com/v1/audio/speech");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                    connection.setRequestProperty("Content-Type", "application/json");

                    String payload = "{\n" +
                            "    \"model\": \"" + model + "\",\n" +
                            "    \"input\": \"" + text + "\",\n" +
                            "    \"voice\": \"" + voice + "\"\n" +
                            "}";

                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    final int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Caminho da pasta Music no armazenamento externo
                        File musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

                        // Criar um diretório exclusivo para os áudios do app
                        File appMusicPath = new File(musicPath, directoryName);
                        if (!appMusicPath.exists()) {
                            appMusicPath.mkdirs();
                        }

                        // Caminho do arquivo de áudio dentro do diretório exclusivo
                        final File audioFile = new File(appMusicPath, audioName + "." + outputFormat);

                        try (InputStream inputStream = connection.getInputStream();
                             FileOutputStream fos = new FileOutputStream(audioFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                            fos.flush();

                            // Atualizar a galeria
                            MediaScannerConnection.scanFile(form, new String[] { audioFile.getAbsolutePath() }, null, null);

                            // Disparar um evento para notificar que o áudio foi salvo
                            form.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                            		SpeechGenerated(audioFile.getAbsolutePath());
                                }
                            });
                        }
                    } else {
                    	form.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            	form.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    	ReportError("Error: HTTP response code " + responseCode);
                                    }
                                });
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    form.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        	ReportError("Error: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
    
    // Método genérico para enviar requisições relacionadas a threads
    private String sendThreadRequest(String method, String urlString, String apiKey, String payload) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("OpenAI-Beta", "assistants=v1");

            // Enable input/output streams for sending and receiving data
            connection.setDoOutput(true);

            // Write the payload to the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the HTTP response code
            int responseCode = connection.getResponseCode();

            // Read the response from the server
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                // Handle error cases and return an appropriate error message
                return "Error: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
 // Método para enviar requisições relacionadas a mensagens em uma thread
    private String sendThreadMessageRequest(String method, String urlString, String apiKey, String payload) {
        try {
            // Define a URL do endpoint da API
            URL url = new URL(urlString);

            // Abre uma conexão com a URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Define o método da requisição (GET, POST, etc.)
            connection.setRequestMethod(method);

            // Define cabeçalhos da requisição
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("OpenAI-Beta", "assistants=v1");

            // Se a requisição for POST ou PUT e tiver um payload, envia o payload
            if (!payload.isEmpty() && (method.equals("POST") || method.equals("PUT"))) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            // Obtém o código de resposta HTTP
            int responseCode = connection.getResponseCode();

            // Lê a resposta do servidor
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line.trim());
                    }
                    return response.toString();
                }
            } else {
                // Trata casos de erro e retorna uma mensagem de erro apropriada
                return "Error: HTTP response code " + responseCode;
            }
        } catch (Exception e) {
            // Trata exceções e retorna uma mensagem de erro
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    private String sendRequest(String prompt, String model, String systemMsg, float temperature, String apiKey) {
        try {
            // Define the API endpoint URL
            URL url = new URL("https://api.openai.com/v1/chat/completions");

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set request headers
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            // Construct the request payload
            String payload = "{\n"
                    + "    \"model\": \"" + model + "\",\n"
                    + "    \"messages\": [\n"
                    + "      {\n"
                    + "      \"role\": \"system\", \n"
                    + "      \"content\": \"" + systemMsg + "\"\n"
                    + "      },\n"
                    + "      {\n"
                    + "        \"role\": \"user\",\n"
                    + "        \"content\": \"" + prompt + "\"\n"
                    + "      }\n"
                    + "    ],\n"
                    + "\"temperature\": " + temperature
                    + "\n  }";

            // Enable input/output streams for sending and receiving data
            connection.setDoOutput(true);

            // Write the payload to the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the HTTP response code
            int responseCode = connection.getResponseCode();

            // Read the response from the server
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                // Handle error cases and return an appropriate error message
                return "Error: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    private String sendImageGenerationRequest(
    		final String oldUrl,
    		final String payload,
    		final ArrayList<String[]> head
    		) 
    {
        try {
            // Define the API endpoint URL
            URL url = new URL(oldUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set request headers
            
            for (String[] header : head) {
                String key = header[0];   // Chave do cabeçalho HTTP
                String value = header[1]; // Valor do cabeçalho HTTP

                // Aqui você pode usar key e value, por exemplo, para configurar os cabeçalhos da conexão
                connection.setRequestProperty(key, value);
            }
            
            // Construct the request payload
            if (flagLog) Log.i(LOG_NAME, "GenerateImage - payload: " + payload);
            
            // Enable input/output streams for sending and receiving data
            connection.setDoOutput(true);

            // Write the payload to the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the HTTP response code
            int responseCode = connection.getResponseCode();

            // Read the response from the server
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                // Handle error cases and return an appropriate error message
                return "Error: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (flagLog) Log.e(LOG_NAME, "Error: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    private String findValueForKey(JSONObject json, String key) {
        try {
            // Verificar se a chave existe no JSON
            if (json.has(key)) {
                // A chave existe, retornar o valor da chave
                return json.getString(key);
            } else {
                // A chave não foi encontrada neste nível, procurar nos objetos aninhados
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String nestedKey = keys.next();
                    if (json.get(nestedKey) instanceof JSONObject) {
                        // Chamar recursivamente para objetos aninhados
                        String nestedValue = findValueForKey(json.getJSONObject(nestedKey), key);
                        if (nestedValue != null) {
                            return nestedValue;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            // Lidar com erros de JSON
            e.printStackTrace();
            ReportError("Error: " + e.getMessage());
        }
        return null;
    }
}
