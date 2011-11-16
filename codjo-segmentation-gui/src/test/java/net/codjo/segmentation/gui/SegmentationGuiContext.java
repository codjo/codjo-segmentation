package net.codjo.segmentation.gui;
import net.codjo.i18n.common.Language;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.i18n.gui.TranslationNotifier;
import net.codjo.mad.gui.MadGuiContext;
import net.codjo.mad.gui.framework.DefaultGuiContext;
import net.codjo.mad.gui.i18n.InternationalizationUtil;

public class SegmentationGuiContext extends DefaultGuiContext {
    public SegmentationGuiContext() {
        MadGuiContext context = new MadGuiContext();

        TranslationManager manager = InternationalizationUtil.retrieveTranslationManager(context);
        TranslationNotifier notifier = InternationalizationUtil.retrieveTranslationNotifier(context);

        manager.addBundle("net.codjo.segmentation.gui.i18n", Language.FR);
        manager.addBundle("net.codjo.segmentation.gui.i18n", Language.EN);

        putProperty(TranslationManager.TRANSLATION_MANAGER_PROPERTY, manager);
        putProperty(TranslationNotifier.TRANSLATION_NOTIFIER_PROPERTY, notifier);
    }
}
