// This is a generated file. Not intended for manual editing.
package ksp.kos.ideaplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static ksp.kos.ideaplugin.psi.KerboScriptTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import ksp.kos.ideaplugin.psi.*;

public class KerboScriptDirectiveImpl extends ASTWrapperPsiElement implements KerboScriptDirective {

  public KerboScriptDirectiveImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KerboScriptVisitor) ((KerboScriptVisitor)visitor).visitDirective(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public KerboScriptLazyglobalDirective getLazyglobalDirective() {
    return findNotNullChildByClass(KerboScriptLazyglobalDirective.class);
  }

}
