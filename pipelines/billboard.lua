addFramebuffer(this, "g_buffer", {
	width = 512,
	height = 512,
	renderbuffers = {
		{ format = "rgba8" },
		{ format = "rgba8" },
		{ format = "rgba8" },
		{ format = "depth24stencil8" }
	}
})


function render()
	main_view = newView(this, "MAIN")
		setPass(this, "DEFERRED")
		enableDepthWrite(this)
		enableRGBWrite(this)
		clear(this, CLEAR_ALL, 0xff00ff00)
		setFramebuffer(this, "g_buffer")
		applyCamera(this, "main")
		setActiveGlobalLightUniforms(this)
		renderModels(this, {main_view})
end
