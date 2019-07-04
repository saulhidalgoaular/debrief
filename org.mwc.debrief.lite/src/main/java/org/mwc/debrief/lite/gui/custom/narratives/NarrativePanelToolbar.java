package org.mwc.debrief.lite.gui.custom.narratives;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.mwc.debrief.lite.gui.LiteStepControl;

import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.Layers.DataListener;
import MWC.TacticalData.NarrativeEntry;
import MWC.TacticalData.NarrativeWrapper;

public class NarrativePanelToolbar extends JPanel
{

  /**
   * Generated Serial Version ID.
   */
  private static final long serialVersionUID = 349058868680231476L;

  public static final String ACTIVE_STATE = "ACTIVE";

  public static final String INACTIVE_STATE = "INACTIVE";

  public static final String STATE_PROPERTY = "STATE";

  public static final String NARRATIVES_PROPERTY = "NARRATIVES";

  public static final String NARRATIVES_REMOVE_COMPLETE_LAYER = "REMOVE_LAYER";

  private String _state = INACTIVE_STATE;

  private final LiteStepControl _stepControl;

  private final List<JComponent> componentsToDisable = new ArrayList<>();

  private final int HEIGHT_FIXED_SIZE = 33;
  /**
   * Maybe this should be inside the abstract model.
   */
  private final DefaultTableModel _narrativeListModel = new DefaultTableModel();

  private final JTable _narrativeList = new JTable();

  private final TreeMap<NarrativeEntry, NarrativeEntryItem> entry2Index = new TreeMap<>();

  private final AbstractNarrativeConfiguration _model;

  private final NarrativePanelToolbar _toolbarInstance;

  private final PropertyChangeListener enableDisableButtonsListener =
      new PropertyChangeListener()
      {

        @Override
        public void propertyChange(final PropertyChangeEvent event)
        {
          if (STATE_PROPERTY.equals(event.getPropertyName()))
          {
            final boolean isActive = ACTIVE_STATE.equals(event.getNewValue());
            for (final JComponent component : componentsToDisable)
            {
              component.setEnabled(isActive);
            }
          }
        }
      };

  private final PropertyChangeListener updatingNarrativesListener =
      new PropertyChangeListener()
      {

        @Override
        public void propertyChange(final PropertyChangeEvent evt)
        {
          if (NARRATIVES_PROPERTY.equals(evt.getPropertyName()))
          {
            if (evt.getNewValue() instanceof NarrativeWrapper)
            {
              final NarrativeWrapper narrativeWrapper = (NarrativeWrapper) evt
                  .getNewValue();

              final Set<NarrativeEntry> toRemove = new TreeSet<>();
              final Set<NarrativeEntry> toAdd = new TreeSet<>();
              // Check difference
              if (_model.getRegisteredNarrativeWrapper().contains(
                  narrativeWrapper))
              {
                final Set<NarrativeEntry> newEntries = new TreeSet<>();
                final Enumeration<Editable> items = narrativeWrapper.elements();
                while (items.hasMoreElements())
                {
                  final Editable thisE = items.nextElement();
                  newEntries.add((NarrativeEntry) thisE);
                }
                for (final NarrativeEntry currentEntry : _model
                    .getCurrentNarrativeEntries(narrativeWrapper))
                {
                  if (!newEntries.contains(currentEntry))
                  {
                    toRemove.add(currentEntry);
                  }
                }
                for (final NarrativeEntry newEntry : newEntries)
                {
                  if (!_model.getCurrentNarrativeEntries(narrativeWrapper)
                      .contains(newEntry))
                  {
                    toAdd.add(newEntry);
                  }
                }
              }
              else
              {
                _model.addNarrativeWrapper(narrativeWrapper);
                final Enumeration<Editable> items = narrativeWrapper.elements();
                while (items.hasMoreElements())
                {
                  final Editable thisE = items.nextElement();
                  toAdd.add((NarrativeEntry) thisE);
                }

              }

              for (final NarrativeEntry entry : toAdd)
              {
                final NarrativeEntryItem entryItem = new NarrativeEntryItem(
                    entry, _model);
                _narrativeListModel.addRow(new NarrativeEntryItem[]
                {entryItem});
                entry2Index.put(entry, entryItem);
                _model.registerNewNarrativeEntry(narrativeWrapper, entry);

              }
              for (final NarrativeEntry entry : toRemove)
              {
                for ( int i = 0 ; i < _narrativeListModel.getRowCount(); i++ )
                {
                  if ( _narrativeListModel.getValueAt(i, 0) == entry2Index.get(entry) )
                  {
                    _narrativeListModel.removeRow(i);
                    break;
                  }
                }
                entry2Index.remove(entry);
              }
              // Sort it.
            }
          }
          else if (NARRATIVES_REMOVE_COMPLETE_LAYER.equals(evt
              .getPropertyName()))
          {
            final NarrativeWrapper wrapperRemoved = (NarrativeWrapper) evt
                .getNewValue();
            final Enumeration<Editable> iteratorToRemove = wrapperRemoved
                .elements();
            while (iteratorToRemove.hasMoreElements())
            {
              final Editable thisE = iteratorToRemove.nextElement();
              if ( entry2Index.containsKey(thisE) )
              {
                for ( int i = 0 ; i < _narrativeListModel.getRowCount(); i++ )
                {
                  if ( _narrativeListModel.getValueAt(i, 0) == entry2Index.get(thisE) )
                  {
                    _narrativeListModel.removeRow(i);
                    entry2Index.remove(thisE);
                    break;
                  }
                }
              }
            }
            _model.removeNarrativeWrapper(wrapperRemoved);
          }

          if (_narrativeListModel.getRowCount() > 0)
          {
            setState(ACTIVE_STATE);
          }
          else
          {
            setState(INACTIVE_STATE);
          }
        }
      };

  private final ArrayList<PropertyChangeListener> stateListeners;

  public NarrativePanelToolbar(final LiteStepControl stepControl,
      final AbstractNarrativeConfiguration model)
  {
    super(new FlowLayout(FlowLayout.LEFT));

    this._narrativeList.setModel(_narrativeListModel);
    this._toolbarInstance = this;
    this._narrativeListModel.addColumn("");
    final TableColumn column = this._narrativeList.getColumnModel().getColumn(
        0);
    column.setCellRenderer(new NarrativeEntryItemRenderer());
    this._narrativeList.addComponentListener(new ComponentAdapter()
    {

      @Override
      public void componentResized(final ComponentEvent e)
      {
        _toolbarInstance.updateRowHeights();
      }

    });

    this._stepControl = stepControl;
    this._model = model;
    init();

    stateListeners = new ArrayList<>(Arrays.asList(enableDisableButtonsListener,
        updatingNarrativesListener));

    model.setRepaintMethod(new Callable<Void>()
    {

      @Override
      public Void call() throws Exception
      {
        _narrativeList.repaint();
        return null;
      }
    });

    this._model.addPropertyChangeListener(new PropertyChangeListener()
    {

      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (NarrativeConfigurationModel.NARRATIVE_HIGHLIGHT.equals(evt
            .getPropertyName()))
        {
          _model.repaintView();
        }
      }
    });
    setState(INACTIVE_STATE);
  }

  protected void checkNewNarratives(final Layers layers)
  {
    final Enumeration<Editable> elem = layers.elements();
    final Set<NarrativeWrapper> loadedNarratives = new TreeSet<>();
    while (elem.hasMoreElements())
    {
      final Editable nextItem = elem.nextElement();
      if (nextItem instanceof NarrativeWrapper)
      {
        final NarrativeWrapper newNarrative = (NarrativeWrapper) nextItem;
        loadedNarratives.add(newNarrative);
        if (!_model.getRegisteredNarrativeWrapper().contains(nextItem))
        {
          _model.addNarrativeWrapper(newNarrative);
          newNarrative.getSupport().addPropertyChangeListener(
              new PropertyChangeListener()
              {

                @Override
                public void propertyChange(final PropertyChangeEvent evt)
                {
                  notifyListenersStateChanged(nextItem, NARRATIVES_PROPERTY,
                      null, nextItem);
                }
              });
          notifyListenersStateChanged(nextItem, NARRATIVES_PROPERTY, null,
              nextItem);
        }
      }
    }

    for (final NarrativeWrapper narrativeWrappersInPanel : _model
        .getRegisteredNarrativeWrapper())
    {
      // Some items has been removed.
      if (!loadedNarratives.contains(narrativeWrappersInPanel))
      {
        notifyListenersStateChanged(narrativeWrappersInPanel,
            NARRATIVES_REMOVE_COMPLETE_LAYER, null, narrativeWrappersInPanel);
      }
    }
  }

  /*
   * private JButton createCommandButton(final String command, final String image) { final ImageIcon
   * icon = Utils.getIcon(image); final JButton button = new JButton(icon);
   * button.setToolTipText(command); return button; }
   */

  private void createDataListeners()
  {
    if (_stepControl != null && _stepControl.getLayers() != null)
    {
      final DataListener registerNarrativeListener = new DataListener()
      {

        @Override
        public void dataExtended(final Layers theData)
        {
          checkNewNarratives(theData);
        }

        @Override
        public void dataModified(final Layers theData, final Layer changedLayer)
        {
          checkNewNarratives(theData);
        }

        @Override
        public void dataReformatted(final Layers theData,
            final Layer changedLayer)
        {
          checkNewNarratives(theData);
        }
      };
      _stepControl.getLayers().addDataExtendedListener(
          registerNarrativeListener);
      _stepControl.getLayers().addDataModifiedListener(
          registerNarrativeListener);
      _stepControl.getLayers().addDataReformattedListener(
          registerNarrativeListener);
    }
  }

  private JToggleButton createJToggleButton(final String command,
      final String image)
  {
    final ImageIcon icon = Utils.getIcon(image);
    final JToggleButton button = new JToggleButton(icon);
    button.setToolTipText(command);
    return button;
  }

  private JComboBox<String> createTracksComboFilter(
      final JSelectTrackFilter selectTrack)
  {
    final JComboBox<String> tracksFilterLabel = new JComboBox<>(new String[]
    {"Sources"});
    tracksFilterLabel.setEnabled(true);
    tracksFilterLabel.addMouseListener(new MouseListener()
    {

      @Override
      public void mouseClicked(final MouseEvent e)
      {
        System.out.println(); // Removing Codacy warning
      }

      @Override
      public void mouseEntered(final MouseEvent e)
      {
        System.out.println(); // Removing Codacy warning
      }

      @Override
      public void mouseExited(final MouseEvent e)
      {
        System.out.println(); // Removing Codacy warning
      }

      @Override
      public void mousePressed(final MouseEvent e)
      {
        if (tracksFilterLabel.isEnabled())
        {
          // Get the event source
          final Component component = (Component) e.getSource();

          selectTrack.show(component, 0, 0);

          // Get the location of the point 'on the screen'
          final Point p = component.getLocationOnScreen();

          selectTrack.setLocation(p.x, p.y + component.getHeight());
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e)
      {
        System.out.println(); // Removing Codacy warning
      }
    });
    return tracksFilterLabel;
  }

  private JComboBox<String> createTypeFilterCombo(
      final JSelectTrackFilter selectTrack, final JSelectTypeFilter typeFilter)
  {
    final JComboBox<String> typeFilterLabel = new JComboBox<>(new String[]
    {"Types"});
    typeFilterLabel.setEnabled(true);
    typeFilterLabel.addMouseListener(new MouseListener()
    {

      @Override
      public void mouseClicked(final MouseEvent e)
      {
        System.out.println(); // Removing Codacy warning
      }

      @Override
      public void mouseEntered(final MouseEvent e)
      {
        System.out.println(); // Removing Codacy warning
      }

      @Override
      public void mouseExited(final MouseEvent e)
      {
        System.out.println(); // Removing Codacy warning
      }

      @Override
      public void mousePressed(final MouseEvent e)
      {
        if (typeFilterLabel.isEnabled())
        {
          // Get the event source
          final Component component = (Component) e.getSource();

          typeFilter.show(component, 0, 0);

          // Get the location of the point 'on the screen'
          final Point p = component.getLocationOnScreen();

          selectTrack.setLocation(p.x, p.y + component.getHeight());
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e)
      {
        System.out.println(); // Removing Codacy warning
      }
    });
    return typeFilterLabel;
  }

  private JToggleButton createWrapButton()
  {
    final JToggleButton wrapTextButton = createJToggleButton("Wrap Text",
        "icons/16/wrap.png");
    wrapTextButton.setSelected(true);
    wrapTextButton.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(final ActionEvent e)
      {
        _model.setWrapping(wrapTextButton.isSelected());
        updateRowHeights();
      }
    });
    return wrapTextButton;
  }
  
  public JTable getNarrativeList()
  {
    return _narrativeList;
  }

  private void init()
  {
    final JSelectTrackFilter selectTrack = new JSelectTrackFilter(_model);

    final JComboBox<String> tracksFilterLabel = createTracksComboFilter(
        selectTrack);

    final JSelectTypeFilter typeFilter = new JSelectTypeFilter(_model);
    final JComboBox<String> typeFilterLabel = createTypeFilterCombo(selectTrack,
        typeFilter);

    final JToggleButton wrapTextButton = createWrapButton();

    /*
     * final JButton copyButton = createCommandButton("Copy Selected Entrey",
     * "icons/16/copy_to_clipboard.png"); copyButton.addActionListener(new ActionListener() {
     *
     * @Override public void actionPerformed(final ActionEvent e) {
     * System.out.println("Copy selected entry not implemented"); } });
     *
     * final JButton addBulkEntriesButton = createCommandButton("Add Bulk Entries",
     * "icons/16/list.png"); addBulkEntriesButton.addActionListener(new ActionListener() {
     *
     * @Override public void actionPerformed(final ActionEvent e) {
     * System.out.println("Add Bulk Entries not implemented"); } });
     *
     * final JButton addSingleEntryButton = createCommandButton("Add Single Entry",
     * "icons/16/add.png"); addBulkEntriesButton.addActionListener(new ActionListener() {
     *
     * @Override public void actionPerformed(final ActionEvent e) {
     * System.out.println("Add single entry not implemented"); } });
     */

    /*
     * add(tracksFilterLabel); add(typeFilterLabel);
     */
    add(wrapTextButton);
    /*
     * add(copyButton); add(addBulkEntriesButton); add(addSingleEntryButton);
     */

    componentsToDisable.addAll(Arrays.asList(new JComponent[]
    {tracksFilterLabel, typeFilterLabel, wrapTextButton/*
                                                        * , copyButton, addBulkEntriesButton,
                                                        * addSingleEntryButton
                                                        */}));

    createDataListeners();
  }

  private void notifyListenersStateChanged(final Object source,
      final String property, final Object oldValue, final Object newValue)
  {
    for (final PropertyChangeListener event : stateListeners)
    {
      event.propertyChange(new PropertyChangeEvent(source, property, oldValue,
          newValue));
    }
  }

  public void setState(final String newState)
  {
    final String oldState = _state;
    this._state = newState;

    if (newState != null && !newState.equals(oldState))
    {
      notifyListenersStateChanged(this, STATE_PROPERTY, oldState, newState);
    }
  }

  public void updateRowHeights()
  {
    for (int row = 0; row < _narrativeList.getRowCount(); row++)
    {
      if (_model.isWrapping())
      {

        int rowHeight = _narrativeList.getRowHeight();

        for (int column = 0; column < _narrativeList.getColumnCount(); column++)
        {
          final Component comp = _narrativeList.prepareRenderer(_narrativeList
              .getCellRenderer(row, column), row, column);
          rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
        }

        _narrativeList.setRowHeight(row, rowHeight);
      }
      else
      {
        _narrativeList.setRowHeight(row, HEIGHT_FIXED_SIZE);
      }
    }
  }

}
