export default {
  name: "ponytail",
  description: "Ponytail plugin for OpenCode",

  async init(context) {
    context.registerSkill({
      name: "ponytail",
      description: "A skill provided by the ponytail plugin",
      async execute(params, context) {
        return "Ponytail plugin is active!";
      },
    });
  },
};
