const User = require('../models/UserModel');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const JWT_SECRET = process.env.JWT_SECRET || 'my_secret';


// kreiranje tokena
const createToken = (user) => {
    return jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: '1d' });
}

exports.login = async (req, res) => {
    const { username, password } = req.body;
    try {
      const user = await User.findOne({ username });
      if (!user) return res.status(401).json({ message: 'Invalid credentials' });
  
      const match = await bcrypt.compare(password, user.password);
      if (!match) return res.status(401).json({ message: 'Invalid credentials' });
  
      const token = createToken(user);
      res.cookie('token', token, {
        secure: true,
        httpOnly: true,
        sameSite: 'Strict',
        maxAge: 24 * 60 * 60 * 1000,
      });
  
      return res.json({ user: { id: user._id, username: user.username, email: user.email } });
    } catch (error) {
      return res.status(500).json({ message: 'Server error' });
    }
}

exports.register = async (req, res) => {
    const { username, email, password } = req.body;
    if (!username || !email || !password) {
      return res.status(400).json({ message: 'All fields are required' });
    }
    try {
      const existsEmail = await User.findOne({ email });
      if (existsEmail) return res.status(400).json({ message: 'Email already in use' });
  
      const existsUsername = await User.findOne({ username });
      if (existsUsername) return res.status(400).json({ message: 'Username already taken' });
  
      const hashedPassword = await bcrypt.hash(password, 10);
      const avatar = 'https://api.dicebear.com/7.x/fun-emoji/svg?seed=1';
  
      const newUser = new User({ username, email, password: hashedPassword, image: avatar });
      await newUser.save();
  
      return res.status(201).json({ user: { id: newUser._id, username: newUser.username, email: newUser.email } });
    } catch (error) {
      return res.status(500).json({ message: 'Server error' });
    }
}

exports.getProfile = async (req, res) => {
    try {
        const user = await User.findById(req.user.id).select('-password').populate('historySimulations');
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }
        return res.status(200).json(user);
    } catch (error) {
        return res.status(500).json({ message: 'Server error' });
    }
}

exports.updateProfilePhoto = async (req, res) => {
    const { imageUrl} = req.body;
    if (!req.user.id || !imageUrl) {
        return res.status(400).json({ message: 'All fields are required' });
    }
    try {
        const user = await User.findById(req.user.id);
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }
        user.imageUrl = imageUrl;
        await user.save();
        return res.status(200).json({ message: 'Profile photo updated successfully' });
    } catch (error) {
        return res.status(500).json({ message: 'Server error' });
    }   
}

exports.updateUsername = async (req, res) => {
    const { username } = req.body;
    if (!req.user.id) {
        return res.status(400).json({ message: 'All fields are required' });
    }
    try {
        const user = await User.findById(req.user.id);
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }
        user.username = username;
        await user.save();
    } catch (error) {
        return res.status(500).json({ message: 'Server error' });
    }
}

exports.updatePasswrord = async (req, res) => {
    const { password, newPassword } = req.body;
    if (!req.user.id || !password || !newPassword) {
        return res.status(400).json({ message: 'All fields are required' });
    }
    try {
        const user = await User.findById(req.user.id);
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }  
        const match = await bcrypt.compare(password, user.password);
        if(match) {
            const hashedPassword = await bcrypt.hash(newPassword, 10);
            user.password = hashedPassword;
            await user.save();
            return res.status(200).json({ message: 'Password updated successfully' });
        }
        return res.status(400).json({ message: 'Wrong password' });
    } catch (error) {
        return res.status(500).json({ message: 'Server error' });
    }
}
