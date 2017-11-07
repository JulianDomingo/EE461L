package observer3;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.event.ChangeEvent;

public class ComplementaryColorPanel extends ColorPanel implements PropertyChangeListener {
    public ComplementaryColorPanel(Color initialColor){
        super(initialColor);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) 
    {
        if (DisplayColors.hueSlider != null && DisplayColors.saturationSlider != null && DisplayColors.brightnessSlider != null) {
            float newHue = (float) DisplayColors.hueSlider.getValue() / 100;
            float newSaturation = (float) DisplayColors.saturationSlider.getValue() / 100;
            float newBrightness = (float) DisplayColors.brightnessSlider.getValue() / 100;
            Color newColor = Color.getHSBColor(newHue, newSaturation, newBrightness);	
            
            this.setColor(newColor);		
            float complementaryHue = newHue - (float)0.5;

            if(complementaryHue < 0) {
                complementaryHue = complementaryHue + 1;
            }

            Color complementaryColor = Color.getHSBColor(complementaryHue, newSaturation, newBrightness);

            this.setColor(complementaryColor);
        } 
    }
}
