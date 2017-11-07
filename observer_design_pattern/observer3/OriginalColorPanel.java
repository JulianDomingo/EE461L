package observer3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.event.*;
import javax.swing.event.ChangeEvent;

public class OriginalColorPanel extends ColorPanel implements ChangeListener {
    public OriginalColorPanel(Color initialColor){
        super(initialColor);
    }

    @Override
    public void stateChanged(ChangeEvent e) 
    {
        if (DisplayColors.hueSlider != null && DisplayColors.saturationSlider != null && DisplayColors.brightnessSlider != null) {
            float newHue = (float) DisplayColors.hueSlider.getValue() / 100;
            float newSaturation = (float) DisplayColors.saturationSlider.getValue() / 100;
            float newBrightness = (float) DisplayColors.brightnessSlider.getValue() / 100;
            Color newColor = Color.getHSBColor(newHue, newSaturation, newBrightness);

            // Fire propertyChange() for ComplementaryColorPanel.class when DisplayColors.complementaryColorPanel changes values.
            this.addPropertyChangeListener(DisplayColors.complementaryColorPanel);
            
            this.setColor(newColor);		
        }
    }
}
