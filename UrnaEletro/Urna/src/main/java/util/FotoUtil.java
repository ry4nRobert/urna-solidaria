package util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;


public class FotoUtil {
    
    public static final int LARGURA = 170;
    public static final int ALTURA = 300;

    // Abre a janela para escolher o arquivo (retorna null se cancelado)
    public static File escolherArquivo(Component pai) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecione a foto do candidato.");
        chooser.setFileFilter(new FileNameExtensionFilter("Imagens (jpg, png)","jpg", "jpeg", "png"));

        return chooser.showOpenDialog(pai) == JFileChooser.APPROVE_OPTION
        ? chooser.getSelectedFile()
        : null;
    }

    //Lê a imagem, ajusta para 170x200 e converte para texto Base64

    public static String arquivoParaBase64(File arquivo) throws Exception {

        BufferedImage original = ImageIO.read(arquivo);

        if (original == null) {
            throw new IllegalArgumentException("O arquivo não é uma imagem.");
        }

        BufferedImage ajustada = new BufferedImage(
            LARGURA, ALTURA, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = ajustada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, LARGURA, ALTURA);
        
        //Preenche o quadrado sem deformar a foto

        double escala = Math.max(
            (double) LARGURA / original.getWidth(),
            (double) ALTURA / original.getHeight()
        );

        int w = (int) Math.round(original.getWidth() * escala);
        int h = (int) Math.round(original.getHeight() * escala);

        g.drawImage(original, (LARGURA - w) / 2, (ALTURA - h) / 2, w, h, null);
        g.dispose();

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        ImageIO.write(ajustada, "jpg", saida);

        return Base64.getEncoder().encodeToString(saida.toByteArray());
    }

    // Converte o texto Base64 de volta para imagem

    public static ImageIcon base64ParaIcon(String base64) {
        
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            return img == null ? null : new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }
}
