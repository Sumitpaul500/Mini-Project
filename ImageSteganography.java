import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;

public class ImageSteganography extends JFrame {
    private JPanel contentPane;
    private JTextArea messageField; // Changed from JTextField to JTextArea
    private JTextArea resultArea;
    private JLabel imagePreview;
    private JLabel statusLabel;
    private BufferedImage originalImage = null;
    private BufferedImage stegoImage = null;
    private final String SIGNATURE = "STEG";
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ImageSteganography frame = new ImageSteganography();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ImageSteganography() {
        setTitle("Image Steganography Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        setLocationRelativeTo(null);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        contentPane.add(mainPanel, BorderLayout.CENTER);
        
        // Control panel (top)
        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        
        // Message input section - now using JTextArea instead of JTextField
        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.setBorder(BorderFactory.createTitledBorder("Secret Message"));
        
        messageField = new JTextArea(3, 20); // 3 rows high initially
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        
        // Add scrollbars to the text area
        JScrollPane messageScrollPane = new JScrollPane(messageField);
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);
        
        controlPanel.add(messagePanel, BorderLayout.CENTER);
        
        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        controlPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Button panel (left)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        JButton openImageBtn = createButton("Open Image");
        JButton embedBtn = createButton("Embed Message");
        JButton extractBtn = createButton("Extract Message");
        JButton saveImageBtn = createButton("Save Image");
        JButton clearBtn = createButton("Clear");
        
        buttonPanel.add(openImageBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(embedBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(extractBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(saveImageBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(clearBtn);
        buttonPanel.add(Box.createVerticalGlue());
        
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        
        // Image preview panel (center)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Image Preview"));
        
        imagePreview = new JLabel("No image loaded", JLabel.CENTER);
        imagePreview.setPreferredSize(new Dimension(400, 300));
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JScrollPane imageScrollPane = new JScrollPane(imagePreview);
        imagePanel.add(imageScrollPane, BorderLayout.CENTER);
        
        mainPanel.add(imagePanel, BorderLayout.CENTER);
        
        // Results panel (bottom)
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setPreferredSize(new Dimension(600, 100));
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(resultsPanel, BorderLayout.SOUTH);
        
        // Action listeners
        openImageBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openImage();
            }
        });
        
        embedBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                embedMessage();
            }
        });
        
        extractBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                extractMessage();
            }
        });
        
        saveImageBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });
        
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(150, 30));
        button.setPreferredSize(new Dimension(150, 30));
        return button;
    }
    
    private void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "bmp"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                originalImage = ImageIO.read(selectedFile);
                if (originalImage != null) {
                    displayImage(originalImage);
                    stegoImage = null;
                    statusLabel.setText("Image loaded: " + selectedFile.getName());
                    resultArea.setText("Image dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight() + 
                                       "\nMaximum message length: " + getMaxTextLength(originalImage) + " characters");
                    
                    // Check if the loaded image contains a hidden message
                    String extractedData = extractData(originalImage);
                    if (extractedData != null && extractedData.startsWith(SIGNATURE)) {
                        resultArea.setText(resultArea.getText() + "\nThis image already contains a hidden message.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Could not load image!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private int getMaxTextLength(BufferedImage image) {
        // Each pixel can store 1 bit in LSB, and we need 8 bits per character
        // We also need to store a 32-bit header for the message length
        int totalPixels = image.getWidth() * image.getHeight();
        return (totalPixels - 32) / 8;
    }
    
    private void displayImage(BufferedImage img) {
        if (img != null) {
            // Scale image for display if needed
            int maxWidth = 400;
            int maxHeight = 300;
            
            double scaleX = (double) maxWidth / img.getWidth();
            double scaleY = (double) maxHeight / img.getHeight();
            double scale = Math.min(scaleX, scaleY);
            
            int scaledWidth = (int) (img.getWidth() * scale);
            int scaledHeight = (int) (img.getHeight() * scale);
            
            Image scaledImage = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(scaledImage));
            imagePreview.setText("");
        } else {
            imagePreview.setIcon(null);
            imagePreview.setText("No image loaded");
        }
    }
    
    private void embedMessage() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please open an image first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String message = messageField.getText();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a message to hide!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // First, check if there's already a hidden message
        String existingMessage = extractData(originalImage);
        if (existingMessage != null && existingMessage.startsWith(SIGNATURE)) {
            // There's already a message, ask user what to do
            String[] options = {"Append", "Overwrite", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this,
                "This image already contains a hidden message. What would you like to do?",
                "Hidden Message Detected",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
            
            if (choice == 0) { // Append
                message = existingMessage.substring(SIGNATURE.length()) + "\n" + message;
            } else if (choice == 2) { // Cancel
                return;
            }
            // If choice == 1 (Overwrite), we just continue with the original message
        }
        
        int maxLength = getMaxTextLength(originalImage);
        if (message.length() > maxLength) {
            JOptionPane.showMessageDialog(this, 
                "Message is too long! Maximum length for this image is " + maxLength + " characters.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            stegoImage = embedData(originalImage, SIGNATURE + message);
            
            displayImage(stegoImage);
            statusLabel.setText("Message embedded successfully!");
            resultArea.setText("Message embedded in image.\nCharacters: " + message.length() + 
                              "\nTo save the image with the hidden message, click 'Save Image'.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error embedding message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private BufferedImage embedData(BufferedImage image, String message) {
        // Create a copy of the original image
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        // Copy pixels from original image
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                output.setRGB(x, y, image.getRGB(x, y));
            }
        }
        
        // Convert message to byte array
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        
        // Embed message length (4 bytes = 32 bits) at the beginning
        int messageLength = messageBytes.length;
        for (int i = 0; i < 32; i++) {
            int x = i % image.getWidth();
            int y = i / image.getWidth();
            
            // Get the bit from message length
            int bit = (messageLength >> (31 - i)) & 1;
            
            // Get the pixel and modify its least significant bit
            int pixel = output.getRGB(x, y);
            pixel = (pixel & ~1) | bit;  // Clear LSB and set it to our bit
            
            output.setRGB(x, y, pixel);
        }
        
        // Embed actual message bytes
        for (int i = 0; i < messageBytes.length; i++) {
            byte b = messageBytes[i];
            
            // Each byte requires 8 pixels to store (1 bit per pixel)
            for (int j = 0; j < 8; j++) {
                int bitIndex = i * 8 + j + 32;  // +32 to skip the header
                int x = bitIndex % image.getWidth();
                int y = bitIndex / image.getWidth();
                
                // Check if we've run out of pixels
                if (y >= image.getHeight()) {
                    throw new RuntimeException("Message is too large for this image");
                }
                
                // Get the bit from the byte
                int bit = (b >> (7 - j)) & 1;
                
                // Get the pixel and modify its least significant bit
                int pixel = output.getRGB(x, y);
                pixel = (pixel & ~1) | bit;  // Clear LSB and set it to our bit
                
                output.setRGB(x, y, pixel);
            }
        }
        
        return output;
    }
    
    private void extractMessage() {
        BufferedImage img = (stegoImage != null) ? stegoImage : originalImage;
        
        if (img == null) {
            JOptionPane.showMessageDialog(this, "Please open an image first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String extractedData = extractData(img);
            
            // Check if the data has our signature
            if (extractedData != null && extractedData.startsWith(SIGNATURE)) {
                String message = extractedData.substring(SIGNATURE.length());
                resultArea.setText("Extracted Message:\n" + message);
                statusLabel.setText("Message extracted successfully!");
            } else {
                resultArea.setText("No hidden message found or the image doesn't contain valid steganographic data.");
                statusLabel.setText("No message found!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error extracting message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private String extractData(BufferedImage image) {
        try {
            // Extract the message length first (32 bits)
            int messageLength = 0;
            for (int i = 0; i < 32; i++) {
                int x = i % image.getWidth();
                int y = i / image.getWidth();
                
                int pixel = image.getRGB(x, y);
                int bit = pixel & 1;  // Get the least significant bit
                
                messageLength = (messageLength << 1) | bit;
            }
            
            // Validate the message length
            if (messageLength <= 0 || messageLength > getMaxTextLength(image)) {
                return null;  // Not a valid steganographic image or corrupted
            }
            
            // Extract the message bytes
            byte[] messageBytes = new byte[messageLength];
            for (int i = 0; i < messageLength; i++) {
                byte b = 0;
                for (int j = 0; j < 8; j++) {
                    int bitIndex = i * 8 + j + 32;  // +32 to skip the header
                    int x = bitIndex % image.getWidth();
                    int y = bitIndex / image.getWidth();
                    
                    // Check if we've run out of pixels
                    if (y >= image.getHeight()) {
                        return null;  // Corrupted data
                    }
                    
                    int pixel = image.getRGB(x, y);
                    int bit = pixel & 1;  // Get the least significant bit
                    
                    b = (byte)((b << 1) | bit);
                }
                messageBytes[i] = b;
            }
            
            // Convert byte array to string
            return new String(messageBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void saveImage() {
        if (stegoImage == null) {
            JOptionPane.showMessageDialog(this, "No image with hidden message to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image As");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        fileChooser.setSelectedFile(new File("stego_image.png"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                // Ensure the file has the .png extension
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getAbsolutePath() + ".png");
                }
                
                ImageIO.write(stegoImage, "png", file);
                statusLabel.setText("Image saved: " + file.getName());
                resultArea.setText(resultArea.getText() + "\nImage saved successfully to: " + file.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearAll() {
        originalImage = null;
        stegoImage = null;
        imagePreview.setIcon(null);
        imagePreview.setText("No image loaded");
        messageField.setText("");
        resultArea.setText("");
        statusLabel.setText("Ready");
    }
}