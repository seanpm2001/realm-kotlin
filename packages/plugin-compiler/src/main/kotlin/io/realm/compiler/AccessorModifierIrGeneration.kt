package io.realm.compiler

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.propertyIfAccessor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class AccessorModifierIrGeneration(val pluginContext: IrPluginContext) {
    fun modifyPropertiesAndReturnSchema(irClass: IrClass) {
        logger("Processing class ${irClass.name}")
        val className = irClass.name.asString()
        SchemaCollector.properties[className] = mutableMapOf()

        irClass.transformChildrenVoid(object : IrElementTransformerVoidWithContext() {
            override fun visitFunctionNew(declaration: IrFunction): IrStatement {
                when {
                    declaration.isRealmString() -> {
                        logger("String property named ${declaration.name} is nullable ${declaration.returnType.isNullable()}")
                        SchemaCollector.properties[className]!![declaration.name.asString()] = Pair("int", declaration.returnType.isNullable())

                        declaration.body?.transformChildrenVoid(object : IrElementTransformerVoid() {
                            override fun visitReturn(expression: IrReturn): IrExpression {
                                return IrBlockBuilder(pluginContext, currentScope?.scope!!, expression.startOffset, expression.endOffset).irBlock {
                                    val irConcat = irConcat()
                                    irConcat.addArgument(irString("Hello "))
                                    irConcat.addArgument(expression.value)
                                    +irReturn(irConcat)
                                }
                            }
                        })
                    }
                    declaration.isRealmLong() -> {
                        logger("Long property named ${declaration.name} is nullable ${declaration.returnType.isNullable()}")
                        SchemaCollector.properties[className]!![declaration.name.asString()] = Pair("int", declaration.returnType.isNullable())
                    }
                    declaration.isRealmInt() -> {
                        logger("Int property named ${declaration.name} is nullable ${declaration.returnType.isNullable()}")
                        SchemaCollector.properties[className]!![declaration.name.asString()] = Pair("int", declaration.returnType.isNullable())
                    }
                    declaration.isRealmBoolean() -> {
                        logger("Boolean property named ${declaration.name} is nullable ${declaration.returnType.isNullable()}")
                        SchemaCollector.properties[className]!![declaration.name.asString()] = Pair("boolean", declaration.returnType.isNullable())
                    }
                    else -> {
                        logger("Type not processed: ${declaration.dump()}")
                    }
                }
                return super.visitFunctionNew(declaration)
            }
        })
    }
}
