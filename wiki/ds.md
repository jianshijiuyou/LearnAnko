  verticalLayout {
                val name = editText("LayoutActyUI") {
                    id = ET_ID
                }
                button("Say Hello") {
                    onClick {
                        ctx.toast("Hello, ${name.text}!")
                        name.textColor = 0xffff0000.toInt()
                    }
                }
            }