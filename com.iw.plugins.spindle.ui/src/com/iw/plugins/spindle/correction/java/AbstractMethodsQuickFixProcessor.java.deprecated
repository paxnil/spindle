package com.iw.plugins.spindle.correction.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;
import org.eclipse.jdt.internal.ui.text.correction.NewMethodCompletionProposal;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.swt.graphics.Image;

public class AbstractMethodsQuickFixProcessor implements IQuickFixProcessor
{

    public boolean hasCorrections(ICompilationUnit unit, int problemId)
    {
        return problemId == IProblem.UndefinedMethod;
    }

    public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
            IProblemLocation[] locations) throws CoreException
    {
        if (locations == null || locations.length == 0)
            return null;

        if (locations == null || locations.length != 1
                || locations[0].getProblemId() != IProblem.UndefinedMethod)
            return null;
        ArrayList proposals = new ArrayList();
        process(context, locations[0], proposals);
//        processTapestry(context, locations[0], proposals);

        if (proposals.isEmpty())
            return null;

        return (IJavaCompletionProposal[]) proposals.toArray(new IJavaCompletionProposal[proposals
                .size()]);
    }

//    private void processTapestry(IInvocationContext context, IProblemLocation problem,
//            Collection proposals)
//    {
//
//        ICompilationUnit cu = context.getCompilationUnit();
//
//        CompilationUnit astRoot = context.getASTRoot();
//        ASTNode selectedNode = problem.getCoveringNode(astRoot);
//
//        if (!(selectedNode instanceof SimpleName))
//            return;
//
//        IResource resource = (IResource) cu.getAdapter(IResource.class);
//
//        if (resource == null)
//            return;
//
//        BaseSpecification spec = getSpecification(resource.getProject(), );
//        
//    }
//    
//    private getSpecification(IProject project, String fullyQualifiedTypeName) {
//        List found = TapestryArtifactManager.getTapestryArtifactManager().
//    }

    private void process(IInvocationContext context, IProblemLocation problem, Collection proposals)
    {
        ICompilationUnit cu = context.getCompilationUnit();

        CompilationUnit astRoot = context.getASTRoot();
        ASTNode selectedNode = problem.getCoveringNode(astRoot);

        if (!(selectedNode instanceof SimpleName))
            return;

        SimpleName nameNode = (SimpleName) selectedNode;

        List arguments;
        Expression sender;

        ASTNode invocationNode = nameNode.getParent();

        if (!(invocationNode instanceof MethodInvocation))
            return;// sorry super method invocation not handled.

        MethodInvocation methodImpl = (MethodInvocation) invocationNode;

        if (isInStaticContext(methodImpl))
            return; // we are not interested in creating static methods.

        arguments = methodImpl.arguments();
        sender = methodImpl.getExpression();

        String methodName = nameNode.getIdentifier();

        try
        {
            createNewAbstractMethodProposal(
                    cu,
                    astRoot,
                    sender,
                    arguments,
                    methodImpl,
                    methodName,
                    proposals);
        }
        catch (JavaModelException e)
        {
            // do nothing
        }
    }

    // answer yes if the method invokation
    private boolean isInStaticContext(MethodInvocation invocationNode)
    {
        Expression expression = invocationNode.getExpression();
        if (expression != null)
        {
            if (expression instanceof Name
                    && ((Name) expression).resolveBinding().getKind() == IBinding.TYPE)
            {
                return true;
            }
        }
        else if (ASTResolving.isInStaticContext(invocationNode))
        {
            return true;
        }
        return false;
    }

    private void createNewAbstractMethodProposal(ICompilationUnit cu, CompilationUnit astRoot,
            Expression sender, List arguments, MethodInvocation methodImpl, String methodName,
            Collection proposals) throws JavaModelException
    { // walk up the tree until we encounter a type, which is the type where the method is being
        // invoked.
        ITypeBinding nodeParentType = Bindings.getBindingOfParentType(methodImpl);
        ITypeBinding binding = null;
        if (sender != null)
            // check - the method is invoked on the result of an expression - use the result type of
            // the expression
            binding = sender.resolveTypeBinding();
        else
            // otherwise - use the type where the method is invoked
            binding = nodeParentType;

        // must be a source file else why are we creating a proposal to insert text?
        if (binding != null && binding.isFromSource())
        {
            // finally the type that we want to add the method to...or is it?
            ITypeBinding senderDeclBinding = binding.getTypeDeclaration();

            // not if the sender is an Annotation or Interface!
            if (senderDeclBinding.isAnnotation() || senderDeclBinding.isInterface())
                return;

            // find the target file - the one to add a method to!
            ICompilationUnit targetCU = ASTResolving.findCompilationUnitForBinding(
                    cu,
                    astRoot,
                    senderDeclBinding);
            if (targetCU != null)
            {
                String label;
                Image image;
                ITypeBinding[] parameterTypes = getParameterTypes(arguments);
                if (parameterTypes != null)
                {
                    String sig = ASTResolving.getMethodSignature(methodName, parameterTypes, false);

                    if (ASTResolving.isUseableTypeInContext(
                            parameterTypes,
                            senderDeclBinding,
                            false)
                            && isAbstract(senderDeclBinding))
                    {
                        label = "create abstract method '" + sig + "'";
                        image = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);

                        proposals.add(new NewAbstractMethodCompletionProposal(label, targetCU,
                                methodImpl, arguments, senderDeclBinding, 5, null, image));
                    }

                    // second option - if the type containing the problem is an inner class (not
                    // static or anonymous) we could propose to
                    // add the abstract method declaration to the outer type.
                    if (senderDeclBinding.isNested() && cu.equals(targetCU) && sender == null
                            && superclassDeclaresMethod(methodName, senderDeclBinding))
                    {

                        ASTNode anonymDecl = astRoot.findDeclaringNode(senderDeclBinding);
                        // find declaration of the sender type
                        if (anonymDecl != null)
                        {
                            senderDeclBinding = Bindings.getBindingOfParentType(anonymDecl
                                    .getParent());
                            //
                            if (!senderDeclBinding.isAnonymous()
                                    && ASTResolving.isUseableTypeInContext(
                                            parameterTypes,
                                            senderDeclBinding,
                                            false) && isAbstract(senderDeclBinding))
                            {
                                label = "create abstract method '" + sig + "' in type '"
                                        + ASTResolving.getTypeSignature(senderDeclBinding) + "'";

                                image = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);

                                proposals.add(new NewAbstractMethodCompletionProposal(label,
                                        targetCU, methodImpl, arguments, senderDeclBinding, 5,
                                        null, image));
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isAbstract(ITypeBinding typeBinding)
    {
        return Modifier.isAbstract(typeBinding.getModifiers());
    }

    private boolean superclassDeclaresMethod(String methodName, ITypeBinding senderDeclBinding)
    {
        return Bindings.findMethodInHierarchy(senderDeclBinding, methodName, (ITypeBinding[]) null) == null;
    }

    private ITypeBinding[] getParameterTypes(List args)
    {
        ITypeBinding[] params = new ITypeBinding[args.size()];
        for (int i = 0; i < args.size(); i++)
        {
            Expression expr = (Expression) args.get(i);
            ITypeBinding curr = Bindings.normalizeTypeBinding(expr.resolveTypeBinding());
            if (curr != null && curr.isWildcardType())
                curr = ASTResolving.normalizeWildcardType(curr, true, expr.getAST());

            if (curr == null)
                curr = expr.getAST().resolveWellKnownType("java.lang.Object"); //$NON-NLS-1$

            params[i] = curr;
        }
        return params;
    }

    class NewAbstractMethodCompletionProposal extends NewMethodCompletionProposal
    {

        Type returnType;

        public NewAbstractMethodCompletionProposal(String label, ICompilationUnit targetCU,
                ASTNode invocationNode, List arguments, ITypeBinding binding, int relevance,
                Type returnType, Image image)
        {
            super(label, targetCU, invocationNode, arguments, binding, relevance, image);
            this.returnType = returnType;
        }

        protected int evaluateModifiers(ASTNode targetTypeDecl)
        {
            return Modifier.PUBLIC | Modifier.ABSTRACT;
        }

        protected ASTRewrite getRewrite() throws CoreException
        {
            ASTNode node = getInvocationNode();
            ITypeBinding senderBinding = getSenderBinding();
            CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(node);
            ASTNode typeDecl = astRoot.findDeclaringNode(senderBinding);
            ASTNode newTypeDecl = null;
            boolean isInDifferentCU;
            if (typeDecl != null)
            {
                isInDifferentCU = false;
                newTypeDecl = typeDecl;
            }
            else
            {
                isInDifferentCU = true;
    			isInDifferentCU= true;
    			astRoot= ASTResolving.createQuickFixAST(getCompilationUnit(), null);
    			newTypeDecl= astRoot.findDeclaringNode(senderBinding);
            }
            if (newTypeDecl != null)
            {
                ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());

                MethodDeclaration newStub = getStub(rewrite, newTypeDecl);

                ChildListPropertyDescriptor property = ASTNodes
                        .getBodyDeclarationsProperty(newTypeDecl);
                List members = (List) newTypeDecl.getStructuralProperty(property);

                int insertIndex;
                if (!isInDifferentCU)
                {
                    insertIndex = findMethodInsertIndex(members, getInvocationNode()
                            .getStartPosition());
                }
                else
                {
                    insertIndex = members.size();
                }
                ListRewrite listRewriter = rewrite.getListRewrite(newTypeDecl, property);
                listRewriter.insertAt(newStub, insertIndex, null);

                return rewrite;
            }
            return null;
        }

        private MethodDeclaration getStub(ASTRewrite rewrite, ASTNode targetTypeDecl)
                throws CoreException
        {
            AST ast = targetTypeDecl.getAST();
            MethodDeclaration decl = ast.newMethodDeclaration();

            SimpleName newNameNode = getNewName(rewrite);

            decl.setConstructor(false);
            addNewModifiers(rewrite, targetTypeDecl, decl.modifiers());

            if (returnType != null)
                decl.setReturnType2(returnType);

            ArrayList takenNames = new ArrayList();
            addNewTypeParameters(rewrite, takenNames, decl.typeParameters());

            decl.setName(newNameNode);

            ITypeBinding senderBinding = getSenderBinding();
            IVariableBinding[] declaredFields = senderBinding.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++)
            { // avoid to take parameter names that are equal to field names
                takenNames.add(declaredFields[i].getName());
            }

            // String bodyStatement= ""; //$NON-NLS-1$
            // if (!isConstructor()) {
            Type returnType = getNewMethodType(rewrite);
            if (returnType == null)
                decl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
            else
                decl.setReturnType2(returnType);

            // if (!senderBinding.isInterface() && returnType != null) {
            // ReturnStatement returnStatement= ast.newReturnStatement();
            // returnStatement.setExpression(ASTNodeFactory.newDefaultExpression(ast, returnType,
            // 0));
            // bodyStatement= ASTNodes.asFormattedString(returnStatement, 0, String.valueOf('\n'));
            // }
            // }

            addNewParameters(rewrite, takenNames, decl.parameters());
            addNewExceptions(rewrite, decl.thrownExceptions());

            Block body = null;
            // if (!senderBinding.isInterface()) {
            // body= ast.newBlock();
            // String placeHolder= CodeGeneration.getMethodBodyContent(getCompilationUnit(),
            // senderBinding.getName(), newNameNode.getIdentifier(), isConstructor(), bodyStatement,
            // String.valueOf('\n'));
            // if (placeHolder != null) {
            // ASTNode todoNode= rewrite.createStringPlaceholder(placeHolder,
            // ASTNode.RETURN_STATEMENT);
            // body.statements().add(todoNode);
            // }
            // }
            decl.setBody(body);

            CodeGenerationSettings settings = JavaPreferencesSettings
                    .getCodeGenerationSettings(getCompilationUnit().getJavaProject());
            if (settings.createComments && !senderBinding.isAnonymous())
            {
                String string = CodeGeneration.getMethodComment(getCompilationUnit(), senderBinding
                        .getName(), decl, null, String.valueOf('\n'));
                if (string != null)
                {
                    Javadoc javadoc = (Javadoc) rewrite.createStringPlaceholder(
                            string,
                            ASTNode.JAVADOC);
                    decl.setJavadoc(javadoc);
                }
            }
            return decl;
        }

        private int findMethodInsertIndex(List decls, int currPos)
        {
            int nDecls = decls.size();
            for (int i = 0; i < nDecls; i++)
            {
                ASTNode curr = (ASTNode) decls.get(i);
                if (curr instanceof MethodDeclaration
                        && currPos < curr.getStartPosition() + curr.getLength())
                {
                    return i + 1;
                }
            }
            return nDecls;
        }

    }

}
