package org.mwc.debrief.lite.gui.custom.narratives;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class NarrativePanelView extends JPanel
{

  /**
   * 
   */
  private static final long serialVersionUID = 1218759123615315561L;

  private final NarrativePanelToolbar _toolbar;
  
  private final AbstractNarrativeConfiguration _model;
  
  public NarrativePanelView(final NarrativePanelToolbar toolbar,
      final AbstractNarrativeConfiguration model)
  {
    super();
    
    _toolbar = toolbar;
    _model = model;
    
    setLayout(new BorderLayout());
    
    add(_toolbar, BorderLayout.NORTH);
    final JPanel mainPanel = new JPanel();
    final JScrollPane scrollPane = new JScrollPane(mainPanel);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    
    final JTextField filterTextField = new JTextField();
    filterTextField.setPreferredSize(new Dimension(300, 20));
    mainPanel.add(filterTextField);
    
    mainPanel.add(toolbar.getNarrativeList());
    
    add(scrollPane, BorderLayout.CENTER);
  }
}
